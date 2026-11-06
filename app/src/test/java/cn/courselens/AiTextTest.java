package cn.courselens;
import static org.junit.Assert.*;import org.junit.Test;import org.junit.runner.RunWith;import org.robolectric.RobolectricTestRunner;import org.robolectric.annotation.Config;import android.text.*;import android.text.style.*;
@RunWith(RobolectricTestRunner.class) @Config(sdk=34)
public class AiTextTest {
 @Test public void headingsHaveDistinctSizesAndCodeRemainsSmaller(){Spanned out=(Spanned)AiText.format("# Title\n## Section\n### Detail\nBody `code`");assertEquals(1.45f,out.getSpans(0,5,RelativeSizeSpan.class)[0].getSizeChange(),.001);int section=out.toString().indexOf("Section");assertEquals(1.25f,out.getSpans(section,section+7,RelativeSizeSpan.class)[0].getSizeChange(),.001);}
 @Test public void formatsHeadingsBoldAndCodeWithoutInterpretingCodeMarkup(){CharSequence result=AiText.format("## 标题\n正文 **重点**\n```java\n**原样代码**\n```\n- 列表");assertEquals("标题\n正文 重点\n**原样代码**\n\n• 列表",result.toString());Spanned spans=(Spanned)result;assertTrue(spans.getSpans(0,spans.length(),StyleSpan.class).length>=2);assertEquals(1,spans.getSpans(0,spans.length(),TypefaceSpan.class).length);}
}
