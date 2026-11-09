package ru.litonchik.edt.themes;

import static org.junit.Assert.*;
import org.junit.Test;
import ru.litonchik.edt.themes.model.*;

public class ReleaseInfoTest {
    static String feed(String version){return "{\"version\":\""+version+"\",\"bundle_version\":\""+version+".202611060000\",\"release_url\":\""+ReleaseInfo.PROJECT+version+"\"}";}
    @Test public void comparesNumericComponents(){assertTrue(ReleaseVersion.parse("0.1.10").compareTo(ReleaseVersion.parse("0.1.9"))>0);assertTrue(ReleaseVersion.parse("1.0.0").compareTo(ReleaseVersion.parse("0.99.999"))>0);assertEquals(0,ReleaseVersion.parse("0.1.30").compareTo(new ReleaseVersion(0,1,30)));}
    @Test public void rejectsNonStableVersions(){for(String value:new String[]{"v0.1.30","0.1","0.1.30.20260101","0.1.30-beta","0.01.30","-1.2.3","2147483648.1.1","0.1.30 ","https://x"})assertThrows(value,IllegalArgumentException.class,() -> ReleaseVersion.parse(value));}
    @Test public void parsesGeneratedFlatSchema(){assertEquals(ReleaseVersion.parse("0.1.31"),ReleaseInfo.parse(" \r\n"+feed("0.1.31")+"\t").version());assertEquals(ReleaseInfo.PROJECT+"0.1.31",new ReleaseInfo(ReleaseVersion.parse("0.1.31")).releaseUri().toString());}
    @Test public void acceptsEscapedStringsAndUnknownStringFields(){String value=feed("0.1.31").replace("{","{\"note\":\"\\u0422\\n\\t\\b\\f\\r\\\\\\\"\\/\",");assertEquals(ReleaseVersion.parse("0.1.31"),ReleaseInfo.parse(value).version());}
    @Test public void rejectsMalformedMetadata(){for(String value:new String[]{"{}","[]",feed("0.1.31")+"junk",feed("0.1.31").replace("}",",}"),feed("0.1.31").replace("{","{\"version\":\"0.1.32\","),feed("0.1.31").replace(".202611060000",".qualifier.bad"),feed("0.1.31").replace(".202611060000",""),feed("0.1.31").replace("releases/tag/v","releases/tag/x"),feed("0.1.31").replace("https://github.com","https://example.com"),feed("0.1.31").replace("{","{\"note\":null,"),feed("0.1.31").replace("{","{\"note\":\"\\uXXXX\","),feed("0.1.31").replace("{","{\"note\":\"line\nline\",")," ".repeat(8193)})assertThrows(value,IllegalArgumentException.class,() -> ReleaseInfo.parse(value));}
    @Test public void sameReleaseIsNotAQualifierUpdate(){assertEquals(0,ReleaseInfo.parse(feed("0.1.30")).version().compareTo(new ReleaseVersion(0,1,30)));assertTrue(ReleaseInfo.parse(feed("0.1.29")).version().compareTo(new ReleaseVersion(0,1,30))<0);}
    @Test public void dailyBudgetPersistsAcrossStarts(){long now=1_000_000_000L;assertEquals(0,UpdateSchedule.delay(now,0));assertEquals(UpdateSchedule.DAY-1000,UpdateSchedule.delay(now+1000,now));assertEquals(0,UpdateSchedule.delay(now+UpdateSchedule.DAY,now));assertEquals(0,UpdateSchedule.delay(now,now+1000));}
}
