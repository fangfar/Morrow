package cn.courselens;

import java.util.*;
import java.util.regex.*;
import org.json.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.*;

/** System formats referenced from WakeUp 3.612 (Apache-2.0); see assets/licenses/wakeup-parsers.txt. */
final class WebCourseParser {
 static final class Result {final String system,courses;Result(String system,String courses){this.system=system;this.courses=courses;}}
 static Result parse(String source)throws Exception {
  if(source==null||source.length()>1000000)throw new IllegalArgumentException("页面过大，请打开个人课表页后再识别。");
  String text=source.trim();
  if(text.startsWith("{")||text.startsWith("["))return json(text);
  Document doc=Jsoup.parse(source);Element pre=doc.selectFirst("body > pre");if(pre!=null&&doc.body().childrenSize()==1){String raw=pre.wholeText().trim();if(raw.startsWith("{")||raw.startsWith("["))return json(raw);}
  Set<String> out=new LinkedHashSet<>();String system="通用表格";
  if(!doc.select("#kblist_table .title").isEmpty()){
   system="新正方列表";for(Element body:doc.select("#kblist_table tbody")){
    Matcher weekday=Pattern.compile("(?:星期|周)([一二三四五六日天])").matcher(body.text());if(!weekday.find())throw bad(system);int day=day(weekday.group());
    for(Element row:body.select("tr"))for(Element title:row.select(".title")){
     Element item=title.parent();while(item!=row&&item.select("p").isEmpty())item=item.parent();if(item.select(".title").size()!=1)throw bad(system);
     Element first=row.selectFirst("td");if(first==null)throw bad(system);String units=first.text().replaceAll("第|节|\\s", "");String time=field(item,"节/周");if(time.isEmpty())time=field(item,"周/节");if(time.isEmpty())time=field(item,"周次");
     add(out,day,title.text(),field(item,"上课地点"),field(item,"教师"),parseWeeks(time.replaceAll("[（(][^()（）]*节[）)]", "")),sections(time,units));
    }
   }
  }else if(!doc.select("#kbtable .kbcontent").isEmpty()){
   system="强智";for(Element table:doc.select("#kbtable"))for(Cell cell:grid(table))if(cell.day>0)for(Element content:cell.element.select(".kbcontent")){
    for(String block:content.html().split("(?i)(?:<br\\s*/?>)?\\s*-{3,}\\s*(?:<br\\s*/?>)?")){
     Element item=Jsoup.parseBodyFragment(block).body();if(item.text().trim().isEmpty())continue;
     Element time=item.selectFirst("[title='周次(节次)']");if(time==null)throw bad(system);
     String raw=time.text(),weeks=raw.replaceAll("[\\[【][^\\]】]*[\\]】]", "").replace("(周)", "周");
     Element name=item.clone();name.select("font,br").remove();String title=SchoolParser.clean(name.text());if(title.isEmpty())throw bad(system);
     add(out,cell.day,title,field(item,"教室"),field(item,"老师"),parseWeeks(weeks),sections(raw,cell.periods));
    }
   }
  }else if((!doc.select(".festival").isEmpty()&&!doc.select("#table1 .title").isEmpty())||!doc.select("#kbgrid_table_0 .title").isEmpty()){
   system="新正方";for(Element table:doc.select("#table1, #kbgrid_table_0"))for(Cell cell:grid(table))if(cell.day>0){
    for(Element title:cell.element.select(".title")){
     Element item=title.parent();while(item!=cell.element&&item.select("[title='节/周'], [title='周/节']").isEmpty())item=item.parent();
     if(item.select(".title").size()!=1)throw bad(system);
     String time=field(item,"节/周");if(time.isEmpty())time=field(item,"周/节");if(time.isEmpty())throw bad(system);
     String weeks=time.replaceAll("[（(][^()（）]*节[）)]", "");
     add(out,cell.day,title.text(),field(item,"上课地点"),field(item,"教师"),parseWeeks(weeks),sections(time,cell.periods));
    }
   }
  }else if(!doc.select("#Table1").isEmpty()){
   system="旧正方";for(Element table:doc.select("#Table1"))for(Cell cell:grid(table))if(cell.day>0&&!cell.element.text().trim().isEmpty()){
    for(String block:cell.element.html().split("(?i)<br\\s*/?>\\s*<br\\s*/?>")){
     List<String> lines=lines(block);if(lines.isEmpty())continue;int time=-1;for(int i=0;i<lines.size();i++)if(lines.get(i).contains("周")&&lines.get(i).contains("节")){time=i;break;}
     if(time<1)throw bad(system);String raw=lines.get(time);Matcher w=Pattern.compile("[\\{｛]([^{}｛｝]+)[\\}｝]").matcher(raw);if(!w.find())throw bad(system);
     String name=lines.get(0),teacher=lines.size()>time+2?lines.get(time+1):"",room=lines.size()>time+1?lines.get(lines.size()-1):"";
     add(out,cell.day,name,room,teacher,parseWeeks(w.group(1)),sections(raw,cell.periods));
    }
   }
  }else{
   for(Element table:doc.select("table"))readList(table,out);
   if(!out.isEmpty())system="课程明细 / URP";
   else {String fallback=SchoolParser.webCourses(source);return new Result(system,fallback);}
  }
  if(out.isEmpty())throw bad(system);return new Result(system,String.join("\n",out));
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
  String text=value.replaceAll("\\s|第|周|[()（）{}｛｝]", "").replace('～','-').replace('~','-').replace('—','-').replace('，',',').replace('、',',');Set<Integer> result=new TreeSet<>();
  String globalParity="";if(text.contains(",")&&(text.endsWith("单")||text.endsWith("双"))){String prefix=value.substring(0,Math.max(value.lastIndexOf(','),Math.max(value.lastIndexOf('，'),value.lastIndexOf('、'))));if(!prefix.contains("周")&&!prefix.contains("单")&&!prefix.contains("双"))globalParity=text.substring(text.length()-1);}
  for(String part:text.split(",",-1)){
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
 private static int day(String value){String s=value.trim().replaceFirst("^(星期|周)", "");if(s.matches("[1-7]"))return Integer.parseInt(s);int day="一二三四五六日天".indexOf(s);return s.length()==1&&day>=0?Math.min(day+1,7):0;}
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
   boolean header=false;for(Cell cell:row){int day=day(cell.element.text());if(day>0&&cell.element.text().matches("(?:星期|周)[一二三四五六日天1-7]")){weekdays.put(cell.column,day);header=true;}}
   if(header)continue;String units="";for(Cell cell:row)if(cell.element.text().matches("第?[0-9,，、~～—\\-]+节")){units=cell.element.text().replaceAll("第|节", "");break;}
   for(Cell cell:row)if(seen.add(cell.element)&&weekdays.containsKey(cell.column)){cell.day=weekdays.get(cell.column);cell.periods=units;result.add(cell);}
  }return result;
 }
 private static String header(String value){String text=value.replaceAll("\\s", "");switch(text){case "课程名":return "课程名称";case "任课教师":case "授课教师":return "教师";case "上课地点":return "教室";case "上课周次":return "周次";case "上课星期":return "星期";case "上课节次":return "节次";default:return text;}}
 private static String cell(List<Cell> row,Map<String,Integer> columns,String key){Integer index=columns.get(key);return index==null||index>=row.size()?"":row.get(index).element.text().trim();}
 private static void readList(Element table,Set<String> out){
  Map<String,Integer> columns=new HashMap<>();for(List<Cell> row:rows(table)){
   if(row.stream().anyMatch(c->header(c.element.text()).equals("课程名称"))&&row.stream().anyMatch(c->header(c.element.text()).equals("周次"))){columns.clear();for(int i=0;i<row.size();i++)columns.put(header(row.get(i).element.text()),i);continue;}
   if(!columns.keySet().containsAll(Arrays.asList("课程名称","星期","节次","周次")))continue;if(row.stream().allMatch(c->c.element.text().trim().isEmpty()))continue;
   String raw=cell(row,columns,"节次"),count=cell(row,columns,"节数");List<int[]> units;
   if(!count.isEmpty()){int start=Integer.parseInt(raw.replaceAll("第|小节|节", "").trim()),length=Integer.parseInt(count);if(length<1)throw bad("URP");units=Collections.singletonList(new int[]{start,start+length-1});}
   else units=sections(raw+ (raw.contains("节")?"":"节"),"");
   add(out,day(cell(row,columns,"星期")),cell(row,columns,"课程名称"),cell(row,columns,"教学楼")+cell(row,columns,"教室"),cell(row,columns,"教师"),parseWeeks(cell(row,columns,"周次")),units);
  }
 }
}
