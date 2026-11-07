package cn.courselens;

import java.util.*;
import java.util.regex.*;
import org.json.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.*;

/** System formats referenced from WakeUp 3.612 (Apache-2.0); see assets/licenses/wakeup-parsers.txt. */
final class WebCourseParser {
 private static final Pattern WEEK_TIME=Pattern.compile("(?:[单双](?:周)?\\s*[:：]?\\s*)?(?:第)?\\d+(?:[-至]\\d+)?(?:\\([单双](?:周)?\\)|[单双])?(?:\\s*[,、]\\s*(?:第)?\\d+(?:[-至]\\d+)?(?:\\([单双](?:周)?\\)|[单双])?)*\\s*周(?:\\s*(?:\\([单双](?:周)?\\)|[单双](?:周)?))?(?=$|[\\s()\\[\\]{},、;:./-])");
 private static final Pattern PERIOD_TIME=Pattern.compile("(?:第)?\\d+(?:\\s*[-,、]\\s*\\d+)*\\s*节(?=$|[\\s()\\[\\]{},、;:./-])");
 static final class Result {final String system,courses;Result(String system,String courses){this.system=system;this.courses=courses;}}
 static Result parse(String source)throws Exception {
  if(source==null||source.length()>1000000)throw new IllegalArgumentException("页面过大，请打开个人课表页后再识别。");
  String text=source.trim();
  if(text.startsWith("{")||text.startsWith("["))return json(text);
  Document doc=Jsoup.parse(source);Element pre=doc.selectFirst("body > pre");if(pre!=null&&doc.body().childrenSize()==1){String raw=pre.wholeText().trim();if(raw.startsWith("{")||raw.startsWith("["))return json(raw);}
  Set<String> out=new LinkedHashSet<>();String system="通用表格";
  if(!doc.select("#kblist_table .title").isEmpty()){
   system="新正方列表";for(Element body:doc.select("#kblist_table tbody")){
    Matcher weekday=Pattern.compile("(?:星期|周)([一二三四五六日天七])").matcher(body.text());if(!weekday.find())throw bad(system);int day=day(weekday.group());
    for(Element row:body.select("tr"))for(Element title:row.select(".title")){
     Element item=title.parent();while(item!=row&&item.select("p").isEmpty())item=item.parent();if(item.select(".title").size()!=1)throw bad(system);
     Element first=row.selectFirst("td");if(first==null)throw bad(system);String units=first.text().replaceAll("第|节|\\s", "");String time=field(item,"节/周");if(time.isEmpty())time=field(item,"周/节");if(time.isEmpty())time=field(item,"周次");
     add(out,day,title.text(),field(item,"上课地点"),field(item,"教师"),parseWeeks(time.replaceAll("[（(][^()（）]*节[）)]", "")),sections(time,units));
    }
   }
  }else if(!doc.select("#kbtable .kbcontent [title='周次(节次)']").isEmpty()){
   system="强智";for(Element table:doc.select("#kbtable"))for(Cell cell:grid(table))if(cell.day>0)for(Element content:cell.element.select(".kbcontent")){
    for(String block:content.html().split("(?i)(?:<br\\s*/?>)?\\s*-{3,}\\s*(?:<br\\s*/?>)?")){
     Element item=Jsoup.parseBodyFragment(block).body();if(item.text().trim().isEmpty())continue;
     Element time=item.selectFirst("[title='周次(节次)']");if(time==null)throw bad(system);
     String raw=time.text(),weeks=raw.replaceAll("[\\[【][^\\]】]*[\\]】]", "").replace("(周)", "周");
     Element name=item.clone();name.select("font,br").remove();String title=SchoolParser.clean(name.text());if(title.isEmpty())throw bad(system);
     add(out,cell.day,title,field(item,"教室"),field(item,"老师"),parseWeeks(weeks),sections(raw,cell.periods));
    }
   }
  }else if(!doc.select("#table1 [title='节/周'], #table1 [title='周/节'], #kbgrid_table_0 [title='节/周'], #kbgrid_table_0 [title='周/节']").isEmpty()){
   system="新正方";for(Element table:doc.select("#table1, #kbgrid_table_0"))for(Cell cell:grid(table))if(cell.day>0){
    for(Element title:cell.element.select(".title")){
     Element item=title.parent();while(item!=cell.element&&item.select("[title='节/周'], [title='周/节']").isEmpty())item=item.parent();
     if(item.select(".title").size()!=1)throw bad(system);
     String time=field(item,"节/周");if(time.isEmpty())time=field(item,"周/节");if(time.isEmpty())throw bad(system);
     String weeks=time.replaceAll("[（(][^()（）]*节[）)]", "");
     add(out,cell.day,title.text(),field(item,"上课地点"),field(item,"教师"),parseWeeks(weeks),sections(time,cell.periods));
    }
   }
  }else if(!doc.select("#Table1").isEmpty()&&doc.select("#Table1").text().matches("(?s).*[\\{｛]第?\\d+.*周.*[\\}｝].*")){
   system="旧正方";for(Element table:doc.select("#Table1"))for(Cell cell:grid(table))if(cell.day>0&&!cell.element.text().trim().isEmpty()){
    for(String block:cell.element.html().split("(?i)<br\\s*/?>\\s*<br\\s*/?>")){
     List<String> lines=lines(block);if(lines.isEmpty())continue;int time=-1;for(int i=0;i<lines.size();i++)if(lines.get(i).contains("周")&&lines.get(i).contains("节")){time=i;break;}
     if(time<1)throw bad(system);String raw=lines.get(time);Matcher w=Pattern.compile("[\\{｛]([^{}｛｝]+)[\\}｝]").matcher(raw);if(!w.find())throw bad(system);
     String name=lines.get(0),teacher=lines.size()>time+2?lines.get(time+1):"",room=lines.size()>time+1?lines.get(lines.size()-1):"";
     add(out,cell.day,name,room,teacher,parseWeeks(w.group(1)),sections(raw,cell.periods));
    }
   }
  }else{return generic(doc);}
  if(out.isEmpty())throw bad(system);return new Result(system,String.join("\n",out));
 }
 static Result parseGeneric(String source){
  if(source==null||source.length()>1000000)throw new IllegalArgumentException("页面过大，请打开个人课表页后再识别。");return generic(Jsoup.parse(source));
 }
 private static Result generic(Document doc){
  doc.select("[data-morrow-hidden=true], [hidden], [aria-hidden=true], script,style,template").remove();
  for(Element element:doc.select("[style]"))if(element.attr("style").toLowerCase(Locale.ROOT).matches("(?s).*(?:display\\s*:\\s*none|visibility\\s*:\\s*(?:hidden|collapse)).*"))element.remove();
  Set<String> out=new LinkedHashSet<>();for(Element table:doc.select("table"))readList(table,out);
  boolean list=!out.isEmpty();int listed=out.size();for(Element table:doc.select("table"))genericGrid(table,out);readCards(doc,out);
  if(!out.isEmpty())return new Result(list?(out.size()>listed?"通用周课表 / 课程明细":"课程明细 / URP"):"通用周课表",String.join("\n",out));
  return new Result("通用表格",SchoolParser.webCourses(doc.outerHtml()));
 }

 private static Result json(String source)throws Exception {
  Object value=new JSONTokener(source).nextValue();if(!(value instanceof JSONObject))throw bad("JSON");JSONObject root=(JSONObject)value;
  if(root.has("kbList"))return new Result("新正方接口",SchoolParser.xzitCourses(source));
  JSONArray dates=root.optJSONArray("dateList");if(dates==null)throw bad("JSON");Set<String> out=new LinkedHashSet<>();
  for(int d=0;d<dates.length();d++){
   JSONArray courses=dates.getJSONObject(d).getJSONArray("selectCourseList");for(int i=0;i<courses.length();i++){
    JSONObject course=courses.getJSONObject(i);JSONArray slots=course.optJSONArray("timeAndPlaceList");if(slots==null)continue;
    for(int s=0;s<slots.length();s++){JSONObject slot=slots.getJSONObject(s);String mask=slot.getString("classWeek");if(!mask.matches("[01]{1,60}"))throw bad("新 URP");List<String> weeks=new ArrayList<>();for(int w=0;w<mask.length();w++)if(mask.charAt(w)=='1'){if(w>=30)throw bad("新 URP");weeks.add(""+(w+1));}
     int start=slot.getInt("classSessions"),count=slot.getInt("continuingSession");if(count<1)throw bad("新 URP");add(out,slot.getInt("classDay"),course.getString("courseName"),slot.optString("campusName","")+slot.optString("teachingBuildingName","")+slot.optString("classroomName",""),course.optString("attendClassTeacher",""),String.join(",",weeks),Collections.singletonList(new int[]{start,start+count-1}));
    }
   }
  }
  if(out.isEmpty())throw bad("新 URP");return new Result("新 URP 接口",String.join("\n",out));
 }
 private static String field(Element root,String title){
  for(Element e:root.select("[title]"))if(e.attr("title").equals(title)){String value=e.text().trim();if(value.isEmpty()&&e.parent()!=null)value=e.parent().text().trim();return value.replaceFirst("^"+Pattern.quote(title)+"[：:]\\s*", "");}
  for(Element e:root.select("p,font")){String value=e.text().trim();if(value.startsWith(title+"：")||value.startsWith(title+":"))return value.substring(title.length()+1).trim();}return "";
 }
 private static IllegalArgumentException bad(String system){return new IllegalArgumentException("已识别为"+system+"，但课程字段不完整或版本暂不支持。请切换到完整个人课表或列表模式后重试。");}
 private static List<String> lines(String html){Element body=Jsoup.parseBodyFragment(html).body();for(Element e:body.select("br"))e.after("\n");List<String> lines=new ArrayList<>();for(String line:body.wholeText().split("\\n")){String text=SchoolParser.clean(line);if(!text.isEmpty())lines.add(text);}return lines;}
 static String parseWeeks(String value){
  String text=value.replaceAll("\\s|第|周|[()（）{}｛｝]", "").replace('～','-').replace('~','-').replace('—','-').replace('，',',').replace('、',',').replace("至","-").replaceAll("([单双])[:：]","$1");Set<Integer> result=new TreeSet<>();
  String globalParity="";if(text.startsWith("单")||text.startsWith("双")){globalParity=text.substring(0,1);text=text.substring(1);if(text.matches(".*[单双].*"))throw bad("课表周次");}
  if(globalParity.isEmpty()&&text.contains(",")&&(text.endsWith("单")||text.endsWith("双"))){String prefix=value.substring(0,Math.max(value.lastIndexOf(','),Math.max(value.lastIndexOf('，'),value.lastIndexOf('、'))));if(!prefix.contains("周")&&!prefix.contains("单")&&!prefix.contains("双"))globalParity=text.substring(text.length()-1);}
  for(String part:text.split(",",-1)){
   if(part.startsWith("单")||part.startsWith("双"))part=part.substring(1)+part.substring(0,1);
   if(!globalParity.isEmpty()&&!part.endsWith("单")&&!part.endsWith("双"))part+=globalParity;
   Matcher match=Pattern.compile("(\\d+)(?:-(\\d+))?([单双])?").matcher(part);if(!match.matches())throw bad("课表周次");int start=Integer.parseInt(match.group(1)),end=match.group(2)==null?start:Integer.parseInt(match.group(2));if(start<1||end>30||end<start)throw bad("课表周次");
   for(int w=start;w<=end;w++)if(match.group(3)==null||w%2==(match.group(3).equals("单")?1:0))result.add(w);
  }
  if(result.isEmpty())throw bad("课表周次");List<String> weeks=new ArrayList<>();for(int w:result)weeks.add(""+w);return String.join(",",weeks);
 }
 private static List<int[]> sections(String text,String fallback){
  if(text.matches(".*\\d+节/周.*"))throw bad("课程节次");
  Matcher match=Pattern.compile("(?:第)?(\\d+(?:\\s*[-,，、~～—]\\s*\\d+)*)\\s*节").matcher(text);String value=match.find()?match.group(1):fallback;if(match.find())throw bad("课程节次");if(value.isEmpty())throw bad("课程节次");
  Set<Integer> values=new TreeSet<>();for(String part:value.replace('，',',').replace('、',',').replace('~','-').replace('～','-').replace('—','-').split(",")){
   String[] pair=part.trim().split("-",-1);if(pair.length>2||!pair[0].trim().matches("\\d+")||(pair.length==2&&!pair[1].trim().matches("\\d+")))throw bad("课程节次");int start=Integer.parseInt(pair[0].trim()),end=pair.length==2?Integer.parseInt(pair[1].trim()):start;if(start<1||end>30||end<start)throw bad("课程节次");for(int i=start;i<=end;i++)values.add(i);
  }
  List<int[]> ranges=new ArrayList<>();for(int valueNumber:values){if(ranges.isEmpty()||ranges.get(ranges.size()-1)[1]+1!=valueNumber)ranges.add(new int[]{valueNumber,valueNumber});else ranges.get(ranges.size()-1)[1]=valueNumber;}return ranges;
 }
 private static void add(Set<String> out,int day,String name,String room,String teacher,String weeks,List<int[]> units){for(int[] range:units){Course course=new Course(day,range[0],range[1],SchoolParser.clean(name),SchoolParser.clean(room),weeks,SchoolParser.clean(teacher));Course.parse(course.encode());out.add(course.encode());}}
 private static int day(String value){String s=value.trim().replaceFirst("^(星期|周)", "");if(s.matches("[1-7]"))return Integer.parseInt(s);int day="一二三四五六日天七".indexOf(s);return s.length()==1&&day>=0?Math.min(day+1,7):0;}
 private static final class Cell {final Element element;final int column;int day;String periods="";Cell(Element e,int c){element=e;column=c;}}
 private static List<List<Cell>> rows(Element table){
  List<List<Cell>> result=new ArrayList<>();Map<Integer,Cell> held=new HashMap<>();Map<Integer,Integer> until=new HashMap<>();int rowIndex=0;
  for(Element row:table.select("tr")){if(row.closest("table")!=table)continue;if(++rowIndex>1000)throw bad("表格");List<Cell> cells=new ArrayList<>();int column=0;
   for(Element e:row.children()){if(!e.tagName().matches("td|th"))continue;while(until.getOrDefault(column,0)>=rowIndex){cells.add(held.get(column));column++;if(column>64)throw bad("表格");}
    int colspan=span(e,"colspan"),rowspan=span(e,"rowspan");Cell cell=new Cell(e,column);for(int c=0;c<colspan;c++){if(column>=64)throw bad("表格");cells.add(cell);held.put(column,cell);until.put(column,rowIndex+rowspan-1);column++;}
   }
   while(until.getOrDefault(column,0)>=rowIndex){cells.add(held.get(column));column++;if(column>64)throw bad("表格");}result.add(cells);
  }return result;
 }
 private static int span(Element cell,String key){String value=cell.attr(key);int count=value.isEmpty()?1:Integer.parseInt(value);if(count<1||count>50)throw bad("合并单元格");return count;}
 private static List<Cell> grid(Element table){
  Map<Integer,Integer> weekdays=new HashMap<>();List<Cell> result=new ArrayList<>();Set<Element> seen=Collections.newSetFromMap(new IdentityHashMap<>());
  for(List<Cell> row:rows(table)){
   boolean header=false;for(Cell cell:row){int day=day(cell.element.text());if(day>0&&cell.element.text().matches("(?:星期|周)[一二三四五六日天七1-7]")){weekdays.put(cell.column,day);header=true;}}
   if(header)continue;String units="";for(Cell cell:row)if(cell.element.text().matches("第?[0-9,，、~～—\\-]+节")){units=cell.element.text().replaceAll("第|节", "");break;}
   for(Cell cell:row)if(seen.add(cell.element)&&weekdays.containsKey(cell.column)){cell.day=weekdays.get(cell.column);cell.periods=units;result.add(cell);}
  }return result;
 }
 private static String normalize(String value){return java.text.Normalizer.normalize(value,java.text.Normalizer.Form.NFKC).replace('\u00a0',' ').replace('～','-').replace('~','-').replace('—','-').replace('–','-').trim();}
 private static String header(String value){String text=normalize(value).replaceAll("\\s|[*＊]", "").replaceAll("\\([^)]*\\)$", "");switch(text){case "课程":case "科目":case "科目名称":case "课程名":return "课程名称";case "老师":case "任课教师":case "授课教师":case "授课老师":return "教师";case "上课地点":case "上课教室":case "地点":return "教室";case "教学周":case "上课周次":return "周次";case "星期几":case "上课星期":return "星期";case "课节":case "上课节次":return "节次";case "上课时间":case "授课时间":case "时间安排":return "时间";default:return text;}}
 private static String cell(List<Cell> row,Map<String,Integer> columns,String key){Integer index=columns.get(key);return index==null||index>=row.size()?"":row.get(index).element.text().trim();}
 private static void readList(Element table,Set<String> out){
  Map<String,Integer> columns=new HashMap<>();for(List<Cell> row:rows(table)){
   if(row.stream().anyMatch(c->header(c.element.text()).equals("课程名称"))&&row.stream().anyMatch(c->Arrays.asList("周次","时间").contains(header(c.element.text())))){columns.clear();for(int i=0;i<row.size();i++)columns.putIfAbsent(header(row.get(i).element.text()),i);continue;}
   if(!columns.containsKey("课程名称"))continue;
   if(!columns.containsKey("时间")&&!columns.keySet().containsAll(Arrays.asList("星期","节次","周次"))){String name=cell(row,columns,"课程名称");if(!name.isEmpty()&&!header(name).equals("课程名称"))throw bad("课程明细缺少时间列");continue;}
   if(row.stream().allMatch(c->empty(c.element.text())))continue;
   if(row.stream().allMatch(c->c.element==row.get(0).element)&&row.get(0).element.text().matches("^(合计|共计|总计|共\\d+门).*$"))continue;
   String combined=normalize(cell(row,columns,"时间"));int weekday=weekdayValue(cell(row,columns,"星期"));if(weekday==0)weekday=weekDay(combined);
   String raw=cell(row,columns,"节次"),count=cell(row,columns,"节数");List<int[]> units;
   if(!count.isEmpty()){int start=Integer.parseInt(raw.replaceAll("第|小节|节", "").trim()),length=Integer.parseInt(count);if(length<1)throw bad("URP");units=Collections.singletonList(new int[]{start,start+length-1});}
   else units=raw.isEmpty()?sections(combined,""):sections(normalize(raw)+(raw.contains("节")?"":"节"),"");
   String weeks=cell(row,columns,"周次");add(out,weekday,cell(row,columns,"课程名称"),cell(row,columns,"教学楼")+cell(row,columns,"教室"),cell(row,columns,"教师"),weeks.isEmpty()?genericWeeks(combined):parseWeeks(normalize(weeks)),units);
  }
 }
 private static boolean empty(String text){return normalize(text).matches("(?:[-—–/\\s]*|无|无课|暂无课程|暂无数据|休息|午休)");}
 private static int weekDay(String text){Matcher match=Pattern.compile("(?:星期|周|礼拜)([一二三四五六日天七1-7])").matcher(normalize(text));if(!match.find())return 0;int value=day(match.group(1));if(match.find())throw bad("通用课表星期");return value;}
 private static int headingDay(String text){String value=normalize(text);Matcher match=Pattern.compile("^(?:星期|周|礼拜)([一二三四五六日天七1-7])").matcher(value);if(!match.find())return 0;String tail=value.substring(match.end()).replaceAll("今天|明天|昨天|[0-9年/月日.()\\-\\s]", "");return tail.isEmpty()?day(match.group(1)):0;}
 private static int weekdayValue(String text){int value=day(normalize(text));return value>0?value:weekDay(text);}
 private static String periodLabel(String text){String value=normalize(text).replaceAll("\\s|第|小节|节", "");return value.matches("\\d+(?:[-,、]\\d+)*")?value:"";}
 private static SortedSet<Integer> periodNumbers(String text){SortedSet<Integer> result=new TreeSet<>();if(text.isEmpty())return result;for(int[] range:sections("",text))for(int i=range[0];i<=range[1];i++)result.add(i);return result;}
 private static String numbers(Collection<Integer> values){List<String> out=new ArrayList<>();for(int value:values)out.add(""+value);return String.join(",",out);}
 private static String genericWeeks(String text){
  Matcher match=WEEK_TIME.matcher(normalize(text));Set<Integer> values=new TreeSet<>();
  while(match.find())for(String week:parseWeeks(match.group()).split(","))values.add(Integer.parseInt(week));
  String remainder=match.replaceAll("").replaceAll("(?:星期|周|礼拜)[一二三四五六日天七1-7]|(?:第)?\\d+(?:\\s*[-,、]\\s*\\d+)*\\s*节", "");
  if(remainder.matches("(?s).*(?:周|待定|未知|\\d).*"))throw bad("通用课表周次");
  if(values.isEmpty())throw bad("通用课表周次");return numbers(values);
 }
 private static void readCards(Document doc,Set<String> out){
  Set<Element> seen=Collections.newSetFromMap(new IdentityHashMap<>());
  for(Element name:doc.select("[data-course-name], .course-name")){
   if(name.closest("table")!=null)continue;Element scope=name;boolean parsed=false;
   while(scope!=null&&scope!=doc.body()){
    Set<String> names=new HashSet<>();for(Element title:scope.select("[data-course-name], .course-name"))names.add(title.hasAttr("data-course-name")?title.attr("data-course-name"):title.text());if(names.size()>1)throw bad("课程卡片");
    if(scope.text().contains("节")&&scope.text().contains("周")){if(seen.add(scope))genericCourse(scope,0,new TreeSet<>(),out);parsed=true;break;}scope=scope.parent();
   }
   if(!parsed)throw bad("课程卡片");
  }
 }
 private static void genericGrid(Element table,Set<String> out){
  List<List<Cell>> tableRows=rows(table);Map<Integer,Integer> days=new LinkedHashMap<>();Map<Integer,String> periodColumns=new LinkedHashMap<>();int heading=-1;
  for(int r=0;r<tableRows.size();r++){
   Map<Integer,Integer> candidates=new LinkedHashMap<>();for(int c=0;c<tableRows.get(r).size();c++){String text=normalize(tableRows.get(r).get(c).element.text());if(text.length()<=30){int d=headingDay(text);if(d>0)candidates.put(c,d);}}
   if(new HashSet<>(candidates.values()).size()>=2){days=candidates;heading=r;break;}
  }
  if(heading<0){
   for(int r=0;r<tableRows.size();r++){Map<Integer,String> candidates=new LinkedHashMap<>();for(int c=1;c<tableRows.get(r).size();c++){String label=periodLabel(tableRows.get(r).get(c).element.text());if(!label.isEmpty())candidates.put(c,label);}if(candidates.size()>=2){periodColumns=candidates;heading=r;break;}}
  }
  if(heading<0)return;boolean transposed=days.isEmpty();int first=Collections.min(transposed?periodColumns.keySet():days.keySet());
  Map<Element,SortedSet<Integer>> cellDays=new LinkedHashMap<>(),cellPeriods=new LinkedHashMap<>();
  for(int r=heading+1;r<tableRows.size();r++){
   List<Cell> row=tableRows.get(r);Set<Integer> repeated=new HashSet<>();for(Cell cell:row){int d=headingDay(cell.element.text());if(d>0)repeated.add(d);}if(repeated.size()>=2)continue;SortedSet<Integer> units=new TreeSet<>();int weekday=0;
   for(int c=0;c<Math.min(first,row.size());c++){String text=row.get(c).element.text();if(transposed){int found=weekDay(text);if(found==0&&tableRows.get(heading).get(0).element.text().contains("星期"))found=day(normalize(text));if(found>0)weekday=found;}else{String label=periodLabel(text);if(!label.isEmpty())units.addAll(periodNumbers(label));}}
   if(transposed&&weekday==0)continue;
   for(int c:transposed?periodColumns.keySet():days.keySet())if(c<row.size()){
    Element element=row.get(c).element;if(empty(element.text()))continue;
    cellDays.computeIfAbsent(element,key->new TreeSet<>()).add(transposed?weekday:days.get(c));cellPeriods.computeIfAbsent(element,key->new TreeSet<>()).addAll(transposed?periodNumbers(periodColumns.get(c)):units);
   }
  }
  for(Element element:cellDays.keySet()){
   if(cellDays.get(element).size()!=1)throw bad("跨星期合并的通用课表");
   for(Element block:courseBlocks(element))genericCourse(block,cellDays.get(element).first(),cellPeriods.get(element),out);
  }
 }
 private static List<Element> courseBlocks(Element cell){return courseBlocks(cell,0);}
 private static List<Element> courseBlocks(Element cell,int depth){
  if(depth>60)throw bad("课程块嵌套过深的课表");
  List<Element> blocks=new ArrayList<>();long headings=cell.children().stream().filter(e->e.is(".course-name, [data-course-name], h1,h2,h3,h4,h5,h6")).count();
  if(headings>1){Element current=null;for(Node node:cell.childNodes()){
   if(node instanceof Element&&((Element)node).is(".course-name, [data-course-name], h1,h2,h3,h4,h5,h6")){current=new Element("div");blocks.add(current);}
   if(current!=null)current.appendChild(node.clone());else if(!node.toString().trim().isEmpty())throw bad("同格多门课程");
  }return blocks;}
  for(Element child:cell.children())if(child.tagName().matches("div|article|li")&&WEEK_TIME.matcher(normalize(child.text())).find())blocks.add(child);
  if(blocks.size()==1){List<Element> nested=courseBlocks(blocks.get(0),depth+1);if(nested.size()>1)return nested;}
  if(blocks.size()>1){for(Node node:cell.childNodes())if(node instanceof TextNode&&!((TextNode)node).text().trim().isEmpty())throw bad("同格多门课程");List<Element> nested=new ArrayList<>();for(Element block:blocks)nested.addAll(courseBlocks(block,depth+1));return nested;}
  String[] parts=cell.html().split("(?i)<hr[^>]*>|(?:<br\\s*/?>\\s*){2,}|-{4,}");if(parts.length==1)return Collections.singletonList(cell);
  blocks.clear();for(String part:parts)if(!empty(Jsoup.parseBodyFragment(part).text()))blocks.add(Jsoup.parseBodyFragment(part).body());return blocks;
 }
 private static void genericCourse(Element element,int weekday,SortedSet<Integer> positions,Set<String> out){
  Element copy=element.clone();for(Element br:copy.select("br"))br.after("\n");for(Element block:copy.select("p,div,li,h1,h2,h3,h4,h5,h6"))block.appendText("\n");
  String raw=normalize(copy.wholeText()).replaceAll("(?<=\\s)(?=(?:课程名称|课程名|科目|课程|授课教师|任课教师|授课老师|教师|老师|上课地点|上课教室|教室|地点|上课周次|周次|节次|上课时间|星期)\\s*[:：])", "\n");
  Map<String,String> labels=new LinkedHashMap<>();List<String> unlabelled=new ArrayList<>();
  for(String line:raw.split("\\n")){
   line=line.trim();if(line.isEmpty())continue;Matcher label=Pattern.compile("^([^:：]{1,8})\\s*[:：]\\s*(.*)$").matcher(line);
   if(label.matches()){
    String key=header(label.group(1));if(Arrays.asList("课程名称","教师","教室","周次","节次","星期","时间").contains(key)){String value=label.group(2).trim();if(value.isEmpty()||labels.containsKey(key))throw bad("通用课表字段");labels.put(key,value);continue;}
   }
   unlabelled.add(line);
  }
  String name=labels.getOrDefault("课程名称","");
  if(name.isEmpty()){
   Element title=element.selectFirst("[data-course-name], .course-name, h1,h2,h3,h4,h5,h6");
   if(title!=null){
    Element clean=title.clone();clean.select("sup,sub,[role=status]").remove();
    boolean blockHint=!clean.ownText().trim().isEmpty()&&clean.children().stream().anyMatch(child->child.tagName().matches("div|p"));
    name=title.hasAttr("data-course-name")?title.attr("data-course-name"):blockHint?clean.ownText():clean.text();
   }
  }
  if(name.isEmpty()&&!unlabelled.isEmpty()&&!unlabelled.get(0).matches(".*(?:\\d+.*[周节]|星期[一二三四五六日天七]).*"))name=unlabelled.remove(0);
  if(name.trim().isEmpty())throw bad("通用课程名称");
  String schedule=labels.getOrDefault("时间","");List<String> details=new ArrayList<>();boolean seenWeeks=false,interveningText=false,seenPeriods=PERIOD_TIME.matcher(schedule).find();
  for(String line:unlabelled)if(!line.equals(name)){
   boolean weekly=WEEK_TIME.matcher(line).find(),periods=PERIOD_TIME.matcher(line).find();
   if(periods&&!weekly&&seenPeriods&&PERIOD_TIME.matcher(line.replaceFirst("^[()\\[\\]\\s]*", "")).lookingAt())throw bad("未能配对周次的重复节次");
   if(weekly||periods&&!seenPeriods||headingDay(line)>0){if(weekly){if(seenWeeks&&interveningText&&element.selectFirst("[data-course-name], .course-name, h1,h2,h3,h4,h5,h6")==null)throw bad("未能分开的同格多门课程");seenWeeks=true;interveningText=false;}
    String timing=timingText(line,details);schedule+="\n"+timing;seenPeriods|=PERIOD_TIME.matcher(timing).find();
   }
   else{details.add(line);if(seenWeeks)interveningText=true;}
  }
  int explicit=labels.containsKey("星期")?weekdayValue(labels.get("星期")):weekDay(schedule);if(weekday==0)weekday=explicit;if(explicit>0&&explicit!=weekday)throw bad("星期与表格位置冲突的课表");if(labels.containsKey("星期")&&explicit==0)throw bad("通用课表星期");
  List<String> schedules=new ArrayList<>();for(String line:schedule.split("\\n"))if(!line.trim().isEmpty())schedules.add(line);
  if(schedules.size()<2||schedules.stream().anyMatch(line->!WEEK_TIME.matcher(line).find()||!PERIOD_TIME.matcher(line).find()))schedules=Collections.singletonList(schedule);
  else if(labels.containsKey("周次")||labels.containsKey("节次"))throw bad("重复的课表时间字段");
  for(String time:schedules){
   String weeks=labels.containsKey("周次")?parseWeeks(labels.get("周次")):genericWeeks(time);
   String units=labels.getOrDefault("节次","");List<int[]> ranges=units.isEmpty()?sections(time,numbers(positions)):sections(units+(units.contains("节")?"":"节"),"");
   if(!positions.isEmpty())for(int[] range:ranges){if(!positions.contains(range[0])||(positions.size()>1&&!positions.contains(range[1])))throw bad("节次与表格位置冲突的课表");}
   for(int[] range:ranges){Course course=new Course(weekday,range[0],range[1],SchoolParser.clean(name),SchoolParser.clean(labels.getOrDefault("教室","")),weeks,SchoolParser.clean(labels.getOrDefault("教师","")),SchoolParser.clean(String.join("；",details)));Course.parse(course.encode());out.add(course.encode());}
  }
 }
 private static String timingText(String line,List<String> details){
  Matcher clocks=Pattern.compile("(?<!\\d)(?:[01]?\\d|2[0-3]):[0-5]\\d\\s*-\\s*(?:[01]?\\d|2[0-3]):[0-5]\\d(?!\\d)").matcher(line);
  while(clocks.find())details.add("上课时刻："+clocks.group());String text=clocks.replaceAll("");
  Matcher tokens=Pattern.compile(WEEK_TIME.pattern()+"|"+PERIOD_TIME.pattern()).matcher(text);
  int start=-1,end=-1;while(tokens.find()){if(start<0)start=tokens.start();end=tokens.end();}if(start<0)return text;
  String prefix=text.substring(0,start).replaceAll("[()\\[\\]{}\\s]", ""),weekday="";Matcher day=Pattern.compile("(?:星期|周|礼拜)[一二三四五六日天七1-7]").matcher(prefix);if(day.find()){weekday=day.group();prefix=day.replaceAll("");}String suffix=text.substring(end).replaceFirst("^[)\\] }\\s]*", "").trim();
  if(!prefix.isEmpty()||WEEK_TIME.matcher(suffix).find()||PERIOD_TIME.matcher(suffix).find()||suffix.matches("(?s).*(?:待定|未知).*" )||suffix.matches("^[,、]\\s*\\d.*"))throw bad("未能分开的课表时间与说明");
  if(!suffix.isEmpty())details.add(suffix);return weekday+" "+text.substring(start,end);
 }
}
