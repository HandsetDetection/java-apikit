package hd3;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Map.Entry;

import junit.framework.TestCase;
import hdapi3.HD3;
import hdapi3.HD3Util;
import hdapi3.Settings;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Ignore;
import org.junit.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class HD3Test extends TestCase {	
	
	private FileInputStream fis;
	
	private HD3 hd3;		
	
	private List<String> notFoundHeaders = new ArrayList<String>(
			Arrays.asList(new String[] 
				{ 
					"Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.0; Trident/4.0; GTB7.1; SLCC1; .NET CLR 2.0.50727; Media Center PC 5.0; InfoPath.2; .NET CLR 3.5.30729; .NET4.0C; .NET CLR 3.0.30729; AskTbFWV5/5.12.2.16749; 978803803", 
					"Mozilla/5.0 (Windows; U; Windows NT 5.1; fr; rv:1.9.2.22) Gecko/20110902 Firefox/3.6.22 ( .NET CLR 3.5.30729) Swapper 1.0.4", 
					"Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.1; Trident/4.0; Sky Broadband; GTB7.1; SeekmoToolbar 4.8.4; Sky Broadband; Sky Broadband; AskTbBLPV5/5.9.1.14019)"								 
				}
			));
			
	
	private Map<String, String> h1 = new HashMap<String, String>() {{
		put("user-agent","Mozilla/5.0 (Linux; U; Android 2.2.2; en-us; SCH-M828C[3373773858] Build/FROYO) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1");
		put("x-wap-profile","http://www-ccpp.tcl-ta.com/files/ALCATEL_one_touch_908.xml");
		put("match","AlcatelOT-908222");
	}};
	
	private Map<String, String> h2 = new HashMap<String, String>() {{
		put("user-agent","Mozilla/5.0 (Linux; U; Android 2.2.2; en-us; SCH-M828C[3373773858] Build/FROYO) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1");
		put("match","SamsungSCH-M828C");
	}};
	
	private Map<String, String> h3 = new HashMap<String, String>() {{
		put("x-wap-profile","http://www-ccpp.tcl-ta.com/files/ALCATEL_one_touch_908.xml");
		put("match","AlcatelOT-90822");
	}};
	
	private Map<String, String> h4 = new HashMap<String, String>() {{
		put("user-agent","Mozilla/5.0 (Linux; U; Android 2.3.3; es-es; GT-P1000N Build/GINGERBREAD) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1");
		put("x-wap-profile","http://wap.samsungmobile.com/uaprof/GT-P1000.xml");
		put("match","SamsungGT-P1000");
	}};
	
	private Map<String, String> h5 = new HashMap<String, String>() {{
		put("user-agent","Opera/9.80 (J2ME/MIDP; Opera Mini/5.21076/26.984; U; en) Presto/2.8.119 Version/10.54");
		put("match","GenericOperaMini");		
	}};
	
	private Map<String, String> h6 = new HashMap<String, String>() {{
		put("user-agent","Opera/9.80 (iPhone; Opera Mini/6.1.15738/26.984; U; tr) Presto/2.8.119 Version/10.54");
		put("match","AppleiPhone");		
	}};
	
	private Map<String, String> h7 = new HashMap<String, String>() {{
		put("user-agent","Mozilla/5.0 (Linux; U; Android 2.1-update1; cs-cz; SonyEricssonX10i Build/2.1.B.0.1) AppleWebKit/530.17 (KHTML, like Gecko) Version/4.0 Mobile Safari/530.17");
		put("match","SonyEricssonX10I");		
	}};
	
	private Map<String, Map<String, String>> map;
		
	private String nokiaN95 = "{\"general_vendor\":\"Nokia\","
			+ "\"general_model\":\"N95\","
			+ "\"general_platform\":\"Symbian\","
			+ "\"general_platform_version\":\"9.2\","
			+ "\"general_browser\":\"\","
			+ "\"general_browser_version\":\"\","
			+ "\"general_image\":\"nokian95-1403496370-0.gif\","
			+ "\"general_aliases\":[],"
			+ "\"general_eusar\":\"0.50\","
			+ "\"general_battery\":[\"Li-Ion 950 mAh\",\"BL-5F\"],"
			+ "\"general_type\":\"Mobile\","
			+ "\"general_cpu\":[\"Dual ARM 11\",\"332Mhz\"],"
			+ "\"design_formfactor\":\"Dual Slide\","
			+ "\"design_dimensions\":\"99 x 53 x 21\","
			+ "\"design_weight\":\"120\","
			+ "\"design_antenna\":\"Internal\","
			+ "\"design_keyboard\":\"Numeric\","
			+ "\"design_softkeys\":\"2\","
			+ "\"design_sidekeys\":[\"Volume\",\"Camera\"],"
			+ "\"display_type\":\"TFT\","
			+ "\"display_color\":\"Yes\","
			+ "\"display_colors\":\"16M\","
			+ "\"display_size\":\"2.6\\\"\","
			+ "\"display_x\":\"240\","
			+ "\"display_y\":\"320\","
			+ "\"display_other\":[],"
			+ "\"memory_internal\":[\"160MB\",\"64MB RAM\",\"256MB ROM\"],"				
			+ "\"memory_slot\":[\"microSD\",\"8GB\",\"128MB\"],"
			+ "\"network\":[\"GSM850\",\"GSM900\",\"GSM1800\",\"GSM1900\",\"UMTS2100\",\"HSDPA2100\",\"Infrared port\",\"Bluetooth 2.0\",\"802.11b\",\"802.11g\",\"GPRS Class 10\",\"EDGE Class 32\"],"
			+ "\"media_camera\":[\"5MP\",\"2592x1944\"],"
			+ "\"media_secondcamera\":[\"QVGA\"],"
			+ "\"media_videocapture\":[\"VGA@30fps\"],"
			+ "\"media_videoplayback\":[\"MPEG4\",\"H.263\",\"H.264\",\"3GPP\",\"RealVideo 8\",\"RealVideo 9\",\"RealVideo 10\"],"
			+ "\"media_audio\":[\"MP3\",\"AAC\",\"AAC+\",\"eAAC+\",\"WMA\"],"
			+ "\"media_other\":[\"Auto focus\",\"Video stabilizer\",\"Video calling\",\"Carl Zeiss optics\",\"LED Flash\"],"
			+ "\"features\":[\"Unlimited entries\",\"Multiple numbers per contact\",\"Picture ID\",\"Ring ID\",\"Calendar\",\"Alarm\",\"To-Do\",\"Document viewer\",\"Calculator\",\"Notes\",\"UPnP\",\"Computer sync\",\"VoIP\",\"Music ringtones (MP3)\",\"Vibration\",\"Phone profiles\",\"Speakerphone\",\"Accelerometer\",\"Voice dialing\",\"Voice commands\",\"Voice recording\",\"Push-to-Talk\",\"SMS\",\"MMS\",\"Email\",\"Instant Messaging\",\"Stereo FM radio\",\"Visual radio\",\"Dual slide design\",\"Organizer\",\"Word viewer\",\"Excel viewer\",\"PowerPoint viewer\",\"PDF viewer\",\"Predictive text input\",\"Push to talk\",\"Voice memo\",\"Games\"],"
			+ "\"connectors\":[\"USB\",\"miniUSB\",\"3.5mm Headphone\",\"TV Out\"]}";
	
	private String AlcatelOT_908222 = "{\"general_vendor\":\"Alcatel\","
			+ "\"general_model\":\"OT-908\","
			+ "\"general_platform\":\"Android\","
			+ "\"general_platform_version\":\"2.2.2\","
			+ "\"general_browser\":\"Android Webkit\","
			+ "\"general_browser_version\":\"4.0\","
			+ "\"general_image\":\"\","
			+ "\"general_aliases\":[\"Alcatel One Touch 908\"],"
			+ "\"general_eusar\":\"\","
			+ "\"general_battery\":[\"Li-Ion 1300 mAh\"],"
			+ "\"general_type\":\"Mobile\","
			+ "\"general_cpu\":[\"600Mhz\"],"
			+ "\"design_formfactor\":\"Bar\","
			+ "\"design_dimensions\":\"110 x 57.4 x 12.4\","
			+ "\"design_weight\":\"120\","
			+ "\"design_antenna\":\"Internal\","
			+ "\"design_keyboard\":\"Screen\","
			+ "\"design_softkeys\":\"\","
			+ "\"design_sidekeys\":[\"Lock/Unlock\",\"Volume\"],"
			+ "\"display_type\":\"TFT\","
			+ "\"display_color\":\"Yes\","
			+ "\"display_colors\":\"262K\","
			+ "\"display_size\":\"2.8\\\"\","
			+ "\"display_x\":\"240\","
			+ "\"display_y\":\"320\","
			+ "\"display_other\":[\"Capacitive\",\"Touch\",\"Multitouch\"],"
			+ "\"memory_internal\":[\"150MB\"],"				
			+ "\"memory_slot\":[\"microSD\",\"microSDHC\",\"32GB\",\"2GB\"],"
			+ "\"network\":[\"GSM850\",\"GSM900\",\"GSM1800\",\"GSM1900\",\"UMTS900\",\"UMTS2100\",\"HSDPA900\",\"HSDPA2100\",\"Bluetooth 3.0\",\"802.11b\",\"802.11g\",\"802.11n\",\"GPRS Class 12\",\"EDGE Class 12\"],"
			+ "\"media_camera\":[\"2MP\",\"1600x1200\"],"
			+ "\"media_secondcamera\":[],"
			+ "\"media_videocapture\":[\"Yes\"],"
			+ "\"media_videoplayback\":[\"MPEG4\",\"H.263\",\"H.264\"],"
			+ "\"media_audio\":[\"MP3\",\"AAC\",\"AAC+\",\"WMA\"],"
			+ "\"media_other\":[\"Geo-tagging\"],"
			+ "\"features\":[\"Unlimited entries\",\"Caller groups\",\"Multiple numbers per contact\",\"Search by both first and last name\",\"Picture ID\",\"Ring ID\",\"Calendar\",\"Alarm\",\"Calculator\",\"Computer sync\",\"OTA sync\",\"Music ringtones (MP3)\",\"Polyphonic ringtones (64 voices)\",\"Vibration\",\"Flight mode\",\"Silent mode\",\"Speakerphone\",\"Accelerometer\",\"Compass\",\"Voice recording\",\"SMS\",\"MMS\",\"Email\",\"Push Email\",\"IM\",\"Stereo FM radio with RDS\",\"SNS integration\",\"Google Search\",\"Maps\",\"Gmail\",\"YouTube\",\"Google Talk\",\"Picasa integration\",\"Organizer\",\"Document viewer\",\"Voice memo\",\"Voice dialing\",\"Predictive text input\",\"Games\"],"
			+ "\"connectors\":[\"USB 2.0\",\"miniUSB\",\"3.5mm Headphone\"]}";
	
	private String AlcatelOT_90822 = "{\"general_vendor\":\"Alcatel\","
			+ "\"general_model\":\"OT-908\","
			+ "\"general_platform\":\"Android\","
			+ "\"general_platform_version\":\"2.2\","
			+ "\"general_browser\":\"\","
			+ "\"general_browser_version\":\"\","
			+ "\"general_image\":\"\","
			+ "\"general_aliases\":[\"Alcatel One Touch 908\"],"
			+ "\"general_eusar\":\"\","
			+ "\"general_battery\":[\"Li-Ion 1300 mAh\"],"
			+ "\"general_type\":\"Mobile\","
			+ "\"general_cpu\":[\"600Mhz\"],"
			+ "\"design_formfactor\":\"Bar\","
			+ "\"design_dimensions\":\"110 x 57.4 x 12.4\","
			+ "\"design_weight\":\"120\","
			+ "\"design_antenna\":\"Internal\","
			+ "\"design_keyboard\":\"Screen\","
			+ "\"design_softkeys\":\"\","
			+ "\"design_sidekeys\":[\"Lock/Unlock\",\"Volume\"],"
			+ "\"display_type\":\"TFT\","
			+ "\"display_color\":\"Yes\","
			+ "\"display_colors\":\"262K\","
			+ "\"display_size\":\"2.8\\\"\","
			+ "\"display_x\":\"240\","
			+ "\"display_y\":\"320\","
			+ "\"display_other\":[\"Capacitive\",\"Touch\",\"Multitouch\"],"
			+ "\"memory_internal\":[\"150MB\"],"				
			+ "\"memory_slot\":[\"microSD\",\"microSDHC\",\"32GB\",\"2GB\"],"
			+ "\"network\":[\"GSM850\",\"GSM900\",\"GSM1800\",\"GSM1900\",\"UMTS900\",\"UMTS2100\",\"HSDPA900\",\"HSDPA2100\",\"Bluetooth 3.0\",\"802.11b\",\"802.11g\",\"802.11n\",\"GPRS Class 12\",\"EDGE Class 12\"],"
			+ "\"media_camera\":[\"2MP\",\"1600x1200\"],"
			+ "\"media_secondcamera\":[],"
			+ "\"media_videocapture\":[\"Yes\"],"
			+ "\"media_videoplayback\":[\"MPEG4\",\"H.263\",\"H.264\"],"
			+ "\"media_audio\":[\"MP3\",\"AAC\",\"AAC+\",\"WMA\"],"
			+ "\"media_other\":[\"Geo-tagging\"],"
			+ "\"features\":[\"Unlimited entries\",\"Caller groups\",\"Multiple numbers per contact\",\"Search by both first and last name\",\"Picture ID\",\"Ring ID\",\"Calendar\",\"Alarm\",\"Calculator\",\"Computer sync\",\"OTA sync\",\"Music ringtones (MP3)\",\"Polyphonic ringtones (64 voices)\",\"Vibration\",\"Flight mode\",\"Silent mode\",\"Speakerphone\",\"Accelerometer\",\"Compass\",\"Voice recording\",\"SMS\",\"MMS\",\"Email\",\"Push Email\",\"IM\",\"Stereo FM radio with RDS\",\"SNS integration\",\"Google Search\",\"Maps\",\"Gmail\",\"YouTube\",\"Google Talk\",\"Picasa integration\",\"Organizer\",\"Document viewer\",\"Voice memo\",\"Voice dialing\",\"Predictive text input\",\"Games\"],"
			+ "\"connectors\":[\"USB 2.0\",\"microUSB\",\"3.5mm Headphone\"]}";
											
	

	private String SamsungSCH_M828C = "{\"general_vendor\":\"Samsung\","
			+ "\"general_model\":\"SCH-M828C'\","
			+ "\"general_platform\":\"Android\","
			+ "\"general_platform_version\":\"2.2.2\","
			+ "\"general_browser\":\"Android Webkit\","
			+ "\"general_browser_version\":\"4.0\","
			+ "\"general_image\":\"samsungsch-m828c-1355919519-0.jpg\","
			+ "\"general_aliases\":[\"Samsung Galaxy Prevail\", \"Samsung Galaxy Precedent\"],"
			+ "\"general_eusar\":\"\","
			+ "\"general_battery\":[\"Li-Ion 1500 mAh\"],"
			+ "\"general_type\":\"Mobile\","
			+ "\"general_cpu\":[\"800Mhz\"],"
			+ "\"design_formfactor\":\"Bar\","
			+ "\"design_dimensions\":\"113 x 57 x 12\","
			+ "\"design_weight\":\"108\","
			+ "\"design_antenna\":\"Internal\","
			+ "\"design_keyboard\":\"Screen\","
			+ "\"design_softkeys\":\"\","
			+ "\"design_sidekeys\":[],"
			+ "\"display_type\":\"TFT\","
			+ "\"display_color\":\"Yes\","
			+ "\"display_colors\":\"262K\","
			+ "\"display_size\":\"3.2\\\"\","
			+ "\"display_x\":\"320\","
			+ "\"display_y\":\"480\","
			+ "\"display_other\":[\"Capacitive\",\"Touch\",\"Multitouch\", \"Touch Buttons\"],"
			+ "\"memory_internal\":[\"117MB\"],"				
			+ "\"memory_slot\":[\"microSD\",\"microSDHC\",\"32GB\",\"2GB\"],"
			+ "\"network\":[\"CDMA800\",\"CDMA1900\",\"Bluetooth 3.0\"],"
			+ "\"media_camera\":[\"2MP\",\"1600x1200\"],"
			+ "\"media_secondcamera\":[],"
			+ "\"media_videocapture\":[\"QVGA\"],"
			+ "\"media_videoplayback\":[\"MP3\",\"WAV\",\"eAAC+\"],"
			+ "\"media_audio\":[\"MP4\",\"H.264\",\"H.263\"],"
			+ "\"media_other\":[\"Geo-tagging\"],"
			+ "\"features\":[\"Unlimited entries\",\"Caller groups\",\"Multiple numbers per contact\",\"Search by both first and last name\",\"Picture ID\",\"Ring ID\",\"Calendar\",\"Alarm\",\"Document viewer\",\"Calculator\",\"Computer sync\",\"OTA sync\",\"Music ringtones (MP3)\",\"Polyphonic ringtones\",\"Vibration\",\"Flight mode\",\"Silent mode\",\"Speakerphone\",\"Accelerometer\",\"Voice dialing\",\"Voice recording\",\"SMS\",\"Threaded viewer\",\"MMS\",\"Email\",\"Push Email\",\"IM\",\"Organizer\",\"Google Search\",\"Maps\",\"Gmail\",\"YouTube\",\"Google Talk\",\"Picasa integration\",\"Voice memo\",\"Predictive text input (Swype)\",\"Games\"],"
			+ "\"connectors\":[\"USB 2.0\",\"microUSB\",\"3.5mm Headphone\"]}";
	
	private String SamsungGT_P1000 = "{\"general_vendor\":\"Samsung\","
			+ "\"general_model\":\"GT-P1000'\","
			+ "\"general_platform\":\"Android\","
			+ "\"general_platform_version\":\"2.3.3\","
			+ "\"general_browser\":\"Android Webkit\","
			+ "\"general_browser_version\":\"4.0\","
			+ "\"general_image\":\"samsunggt-p1000-1368755043-0.jpg\","
			+ "\"general_aliases\":[\"Samsung Galaxy Tab\"],"
			+ "\"general_eusar\":\"1.07\","
			+ "\"general_battery\":[\"Li-Ion 4000 mAh\"],"
			+ "\"general_type\":\"Tablet\","
			+ "\"general_cpu\":[\"1000Mhz\"],"
			+ "\"design_formfactor\":\"Bar\","
			+ "\"design_dimensions\":\"190.1 x 120.45 x 11.98\","
			+ "\"design_weight\":\"380\","
			+ "\"design_antenna\":\"Internal\","
			+ "\"design_keyboard\":\"Screen\","
			+ "\"design_softkeys\":\"\","
			+ "\"design_sidekeys\":[],"
			+ "\"display_type\":\"TFT\","
			+ "\"display_color\":\"Yes\","
			+ "\"display_colors\":\"16M\","
			+ "\"display_size\":\"7\\\"\","
			+ "\"display_x\":\"1024\","
			+ "\"display_y\":\"600\","
			+ "\"display_other\":[\"Capacitive\",\"Touch\",\"Multitouch\", \"Touch Buttons\", \"Gorilla Glass\", \"TouchWiz\"],"
			+ "\"memory_internal\":[\"16GB\",\"32GB\",\"512MB RAM\"],"				
			+ "\"memory_slot\":[\"microSD\",\"microSDHC\",\"32GB\"],"
			+ "\"network\":[\"GSM850\",\"GSM900\",\"GSM1800\", \"GSM1900\", \"UMTS900\", \"UMTS1900\", \"UMTS2100\", \"HSDPA900\", \"HSDPA1900\", \"HSDPA2100\", \"Bluetooth 3.0\", \"802.11b\",  \"802.11g\",  \"802.11n\",  \"GPRS\",  \"EDGE\",],"
			+ "\"media_camera\":[\"3.15MP\",\"2048x1536\"],"
			+ "\"media_secondcamera\":[\"1.3MP\"],"
			+ "\"media_videocapture\":[\"720x480@30fps\"],"
			+ "\"media_videoplayback\":[\"MPEG4\",\"H.264\",\"DivX\", \"XviD\"],"
			+ "\"media_audio\":[\"MP3\",\"AAC\",\"FLAC\",\"WMA\",\"WAV\",\"AMR\",\"OGG\",\"MIDI\"],"
			+ "\"media_other\":[\"Auto focus\",\"Video calling\",\"Geo-tagging\",\"LED Flash\"],"
			+ "\"features\":[\"Unlimited entries\",\"Caller groups\",\"Multiple numbers per contact\",\"Search by both first and last name\",\"Picture ID\",\"Ring ID\",\"Calendar\",\"Alarm\",\"Document viewer\",\"Calculator\",\"DLNA\",\"Computer sync\",\"OTA sync\",\"Music ringtones (MP3)\",\"Flight mode\",\"Silent mode\",\"Speakerphone\",\"Accelerometer\",\"Voice commands\",\"Voice recording\",\"SMS\",\"Threaded viewer\",\"MMS\",\"Email\",\"Push Mail\",\"IM\",\"RSS\",\"Social networking integration\",\"Full HD video playback\",\"Up to 7h movie playback\",\"Organizer\",\"Image/video editor\",\"Thinkfree Office\",\"Word viewer\",\"Excel viewer\",\"PowerPoint viewer\",\"PDF viewer\",\"Google Search\",\"Maps\",\"Gmail\",\"YouTube\",\"Google Talk\",\"Picasa integration\",\"Readers/Media/Music Hub\",\"Voice memo\",\"Voice dialing\",\"Predictive text input (Swype)\",\"Games\"],"
			+ "\"connectors\":[\"USB\",\"3.5mm Headphone\",\"3.5mm Headphone\",\"TV Out\",\"MHL\"]}";
	
	private String GenericOperaMini = "{\"general_vendor\":\"Generic\","
			+ "\"general_model\":\"Opera Mini 5\","
			+ "\"general_platform\":\"\","
			+ "\"general_platform_version\":\"\","
			+ "\"general_browser\":\"Opera Mini\","
			+ "\"general_browser_version\":\"5.2\","
			+ "\"general_image\":\"\","
			+ "\"general_aliases\":[],"
			+ "\"general_eusar\":\"\","
			+ "\"general_battery\":[],"
			+ "\"general_type\":\"Mobile\","
			+ "\"general_cpu\":[],"
			+ "\"design_formfactor\":\"\","
			+ "\"design_dimensions\":\"\","
			+ "\"design_weight\":\"\","
			+ "\"design_antenna\":\"\","
			+ "\"design_keyboard\":\"\","
			+ "\"design_softkeys\":\"\","
			+ "\"design_sidekeys\":[],"
			+ "\"display_type\":\"TFT\","
			+ "\"display_color\":\"\","
			+ "\"display_colors\":\"\","
			+ "\"display_size\":\"\","
			+ "\"display_x\":\"\","
			+ "\"display_y\":\"\","
			+ "\"display_other\":[],"
			+ "\"memory_internal\":[],"				
			+ "\"memory_slot\":[],"
			+ "\"network\":[],"
			+ "\"media_camera\":[],"
			+ "\"media_secondcamera\":[],"
			+ "\"media_videocapture\":[],"
			+ "\"media_videoplayback\":[],"
			+ "\"media_audio\":[],"
			+ "\"media_other\":[],"
			+ "\"features\":[],"
			+ "\"connectors\":[]}";
	
	private String AppleiPhone = "{\"general_vendor\":\"Apple\","
			+ "\"general_model\":\"iPhone\","
			+ "\"general_platform\":\"iOS\","
			+ "\"general_image\":\"apple^iphone.jpg\","															
			+ "\"general_aliases\":[],"
			+ "\"general_eusar\":\"0.97\","
			+ "\"general_battery\":[\"Li-Ion 1300 mAh\"],"
			+ "\"general_type\":\"Mobile\","
			+ "\"general_cpu\":[\"ARM 11\",\"412Mhz\"],"
			+ "\"design_formfactor\":\"Bar\","
			+ "\"design_dimensions\":\"115 x 61 x 11.6\","
			+ "\"design_weight\":\"135\","
			+ "\"design_antenna\":\"Internal\","
			+ "\"design_keyboard\":\"Screen\","
			+ "\"design_softkeys\":\"\","
			+ "\"design_sidekeys\":[\"Volume\"],"
			+ "\"display_type\":\"TFT\","
			+ "\"display_color\":\"Yes\","
			+ "\"display_colors\":\"16M\","
			+ "\"display_size\":\"3.5\\\"\","
			+ "\"display_x\":\"320\","
			+ "\"display_y\":\"480\","
			+ "\"display_other\":[\"Capacitive\",\"Touch\",\"Multitouch\",\"Gorilla Glass\"],"
			+ "\"memory_internal\":[\"4GB\",\"8GB\",\"16GB RAM\"],"				
			+ "\"memory_slot\":[],"
			+ "\"network\":[\"GSM850\",\"GSM900\",\"GSM1800\",\"GSM1900\",\"Bluetooth 2.0\",\"802.11b\",\"802.11g\",\"GPRS\",\"EDGE\"],"
			+ "\"media_camera\":[\"2MP\",\"1600x1200\"],"
			+ "\"media_secondcamera\":[],"
			+ "\"media_videocapture\":[],"
			+ "\"media_videoplayback\":[\"MPEG4\",\"H.264\"],"
			+ "\"media_audio\":[\"MP3\",\"AAC\",\"WAV\"],"
			+ "\"media_other\":[],"
			+ "\"features\":[\"Unlimited entries\",\"Multiple numbers per contact\",\"Picture ID\",\"Ring ID\",\"Calendar\",\"Alarm\",\"Document viewer\",\"Calculator\",\"Timer\",\"Stopwatch\",\"Computer sync\",\"OTA sync\",\"Polyphonic ringtones\",\"Vibration\",\"Phone profiles\",\"Flight mode\",\"Silent mode\",\"Speakerphone\",\"Accelerometer\",\"Voice recording\",\"Light sensor\",\"Proximity sensor\",\"SMS\",\"Threaded viewer\",\"Email\",\"Google Maps\",\"Audio/video player\",\"Games\"],"
			+ "\"connectors\":[\"USB\",\"3.5mm Headphone\",\"TV Out\"],"
			+ "\"general_platform_version\":\"\","
			+ "\"general_browser\":\"Opera Mini\","
			+ "\"general_browser_version\":\"6.1\"}";
	
	private String SonyEricssonX10I = "[{\"general_vendor\":\"SonyEricsson\","
			+ "\"general_model\":\"X10I\","
			+ "\"general_platform\":\"Android\","
			+ "\"general_platform_version\":\"2.1.1\","
			+ "\"general_browser\":\"Android Webkit\","
			+ "\"general_browser_version\":\"4.0\","
			+ "\"general_image\":\"\","
			+ "\"general_aliases\":[\"SonyEricsson Xperia X10\",\"SonyEricsson X10\"],"
			+ "\"general_eusar\":\"\","
			+ "\"general_battery\":[\"Li-Po 1500 mAh\",\"BST-41\"],"
			+ "\"general_type\":\"Mobile\","
			+ "\"general_cpu\":[\"1000Mhz\"],"
			+ "\"design_formfactor\":\"Bar\","
			+ "\"design_dimensions\":\"119 x 63 x 13\","
			+ "\"design_weight\":\"135\","
			+ "\"design_antenna\":\"Internal\","
			+ "\"design_keyboard\":\"Screen\","
			+ "\"design_softkeys\":\"\","
			+ "\"design_sidekeys\":[\"Volume\",\"Camera\"],"
			+ "\"display_type\":\"TFT\","
			+ "\"display_color\":\"Yes\","
			+ "\"display_colors\":\"65K\","
			+ "\"display_size\":\"4\\\"\","
			+ "\"display_x\":\"480\","
			+ "\"display_y\":\"854\","
			+ "\"display_other\":[\"Capacitive\",\"Touch\",\"Multitouch\"],"
			+ "\"memory_internal\":[\"1GB\",\"384MB RAM\"],"				
			+ "\"memory_slot\":[\"microSD\",\"microSDHC\",\"32GB\",\"8GB\"],"
			+ "\"network\":[\"GSM850\",\"GSM900\",\"GSM1800\",\"GSM1900\",\"UMTS900\",\"UMTS1700\",\"UMTS2100\",\"HSDPA900\",\"HSDPA1700\",\"HSDPA2100\",\"Bluetooth 2.1\",\"802.11b\",\"802.11g\",\"GPRS Class 10\",\"EDGE Class 10\"],"
			+ "\"media_camera\":[\"8MP\",\"3264x2448\"],"
			+ "\"media_secondcamera\":[],"
			+ "\"media_videocapture\":[\"WVGA@30fps\"],"
			+ "\"media_videoplayback\":[\"MPEG4\"],"
			+ "\"media_audio\":[\"MP3\",\"AAC\",\"AAC+\",\"WMA\",\"WAV\"],"
			+ "\"media_other\":[\"Auto focus\",\"Image stabilizer\",\"Video stabilizer\",\"Face detection\",\"Smile detection\",\"Digital zoom\",\"Geo-tagging\",\"Touch focus\",\"LED Flash\"],"
			+ "\"features\":[\"Unlimited entries\",\"Caller groups\",\"Multiple numbers per contact\",\"Search by both first and last name\",\"Picture ID\",\"Ring ID\",\"Calendar\",\"Alarm\",\"Document viewer\",\"Calculator\",\"World clock\",\"Stopwatch\",\"Notes\",\"Computer sync\",\"OTA sync\",\"Music ringtones (MP3)\",\"Polyphonic ringtones\",\"Vibration\",\"Flight mode\",\"Silent mode\",\"Speakerphone\",\"Voice recording\",\"Accelerometer\",\"Compass\",\"Timescape/Mediascape UI\",\"SMS\",\"Threaded viewer\",\"MMS\",\"Email\",\"Push email\",\"IM\",\"Google Search\",\"Maps\",\"Gmail\",\"YouTube\",\"Google Talk\",\"Facebook and Twitter integration\",\"Voice memo\",\"Games\"],"
			+ "\"connectors\":[\"USB 2.0\",\"microUSB\",\"3.5mm Headphone\"]}]";

	
	private String Device_10 = "{\"Device\":{\"_id\":\"10\","
			+ "\"hd_specs\":{\"general_vendor\":\"Samsung\","
			+ "\"general_model\":\"SPH-A680\","
			+ "\"general_platform\":\"\","
			+ "\"general_platform_version\":\"\","
			+ "\"general_browser\":\"\","
			+ "\"general_browser_version\":\"\","
			+ "\"general_image\":\"samsungsph-a680-1403617960-0.jpg\","
			+ "\"general_aliases\":[\"Samsung VM-A680\"],"
			+ "\"general_eusar\":\"\","
			+ "\"general_battery\":[\"Li-Ion 900 mAh\"],"
			+ "\"general_type\":\"Mobile\","
			+ "\"general_cpu\":[],"
			+ "\"design_formfactor\":\"Clamshell\","
			+ "\"design_dimensions\":\"83 x 46 x 24\","
			+ "\"design_weight\":\"96\","
			+ "\"design_antenna\":\"Internal\","
			+ "\"design_keyboard\":\"Numeric\","
			+ "\"design_softkeys\":\"2\","
			+ "\"design_sidekeys\":[],"
			+ "\"display_type\":\"TFT\","
			+ "\"display_color\":\"Yes\","
			+ "\"display_colors\":\"65K\","
			+ "\"display_size\":\"\","
			+ "\"display_x\":\"128\","
			+ "\"display_y\":\"160\","
			+ "\"display_other\":[\"Second External TFT\"],"
			+ "\"memory_internal\":[],"
			+ "\"memory_slot\":[],"
			+ "\"network\":[\"CDMA800\",\"CDMA1900\",\"AMPS800\"],"
			+ "\"media_camera\":[\"VGA\",\"640x480\"],"
			+ "\"media_secondcamera\":[],"
			+ "\"media_videocapture\":[\"Yes\"],"
			+ "\"media_videoplayback\":[],"
			+ "\"media_audio\":[],"
			+ "\"media_other\":[\"Exposure control\",\"White balance\",\"Multi shot\",\"Self-timer\",\"LED Flash\"],"
			+ "\"features\":[\"300 entries\",\"Multiple numbers per contact\",\"Picture ID\",\"Ring ID\",\"Calendar\",\"Alarm\",\"To-Do\",\"Calculator\",\"Stopwatch\",\"SMS\",\"T9\",\"Computer sync\",\"Polyphonic ringtones (32 voices)\",\"Vibration\",\"Voice dialing (Speaker independent)\",\"Voice recording\",\"TTY\\/TDD\",\"Games\"],"
			+ "\"connectors\":[\"USB\"]}}}";
	
	protected void setUp() {		
		try {
			fis = new FileInputStream("hdapi_config.properties"); 
			Settings.init(fis);					
			fis.close();
			map = new HashMap<String, Map<String, String>>();
			map.put("h1", h1);
			map.put("h2", h2);
			map.put("h3", h3);
			map.put("h4", h4);
			map.put("h5", h5);
			map.put("h6", h6);
			map.put("h7", h7);
			hd3 = new HD3();			
		} catch (Exception e) {
			e.printStackTrace();
		} 		
	}
	
	@Test
	public void testUsernameRequired() {
		hd3.setUsername("");		
		assertEquals("",hd3.getUsername());
	}
	
	@Test
	public void testSecretRequired() {
		hd3.setSecret("");
		assertEquals("",hd3.getSecret());
	}	
	
	@Test
	public void testPassedConfig() {			
		hd3.setUsername("jones");
		hd3.setSecret("jango");
		hd3.setSiteId("78");
		hd3.setProxyAddress("127.0.0.1");
		hd3.setProxyPort(8080);
		hd3.setProxyUsername("bob");
		hd3.setProxyPassword("123abc");						
		assertEquals(hd3.getUsername(), SecretConfig.getUserName("jones"));
		assertEquals(hd3.getSecret(), SecretConfig.getSecret("jango"));
		assertEquals(hd3.getSiteId(), SecretConfig.getSiteId("78"));
		assertEquals(hd3.getProxyAddress(), "127.0.0.1");
		assertEquals(hd3.getProxyPort(), 8080);
		assertEquals(hd3.getProxyUsername(), "bob");
		assertEquals(hd3.getProxyPassword(), "123abc");
	}
	
	@Test
	public void testDefaultFileConfig() {
		hd3.setUseProxy(false);
		hd3.setUseLocal(false);		
		assertNotNull(hd3.getUsername());
		assertNotNull(hd3.getSecret());
		assertNotNull(hd3.getSiteId());
		assertNotNull(hd3.getApiServer());
	}
	
	@Test
	public void testDefaultSetup() {
		String header = "Mozilla/5.0 (SymbianOS/9.2; U; Series60/3.1 NokiaN95-3/20.2.011 Profile/MIDP-2.0 Configuration/CLDC-1.1 ) AppleWebKit/413";
		String profile = "http://nds1.nds.nokia.com/uaprof/NN95-1r100.xml";
		String ipaddress = "127.0.0.1";		
				
		JsonObject data = new JsonObject();
		data.addProperty("user-agent", header);
		data.addProperty("x-wap-profile", profile);
		data.addProperty("ipaddress", ipaddress);		
		
		hd3.setup(null, ipaddress, "http://localhost");
		hd3.setUseLocal(false);
		hd3.addDetectVar("user-agent", header);
		hd3.addDetectVar("x-wap-profile", profile);		
		
		JsonObject result = hd3.getDetectRequest();		
		assertEquals(data.get("user-agent"), HD3Util.get("user-agent", result));
		assertEquals(data.get("x-wap-profile"), HD3Util.get("x-wap-profile", result));
		assertEquals(data.get("ipaddress"), HD3Util.get("ipaddress", result));		
	}
	
	@Test
	public void testManualSetup() {
		String header = "Mozilla/5.0 (SymbianOS/9.2; U; Series60/3.1 NokiaN95-3/20.2.011 Profile/MIDP-2.0 Configuration/CLDC-1.1 ) AppleWebKit/413";
		String profile = "http://nds1.nds.nokia.com/uaprof/NN95-1r100.xml";		
		JsonObject json = new JsonObject();
		json.addProperty("user-agent", header);
		json.addProperty("x-wap-profile", profile);						
		hd3.addDetectVar("user-agent", header);
		hd3.addDetectVar("x-wap-profile", profile);		
		JsonObject result = hd3.getDetectRequest();		
		assertEquals(json.get("user-agent"), HD3Util.get("user-agent", result));
		assertEquals(json.get("x-wap-profile"), HD3Util.get("x-wap-profile", result));
	}
	
	@Test
	public void testInvalidCredentials() {
		hd3.setUsername(SecretConfig.getUserName("jones"));
		hd3.setSecret(SecretConfig.getSecret("jipple"));
		hd3.setUseLocal(false);
		hd3.setSiteId(SecretConfig.getSiteId("57"));					
		boolean reply = hd3.deviceVendors();
		assertFalse(reply);
		assertEquals("200", HD3Util.get("status", hd3.getReply()).toString() );
	} 
	
		
	@Test
	public void deviceVendors(boolean local, boolean proxy) {
		List<String> vendors = 
				new ArrayList<String>(Arrays.asList(new String[] { "Apple", "Sony", "Samsung", "Nokia", "LG", "HTC", "Karbonn" }));
		hd3.setUseLocal(local);
		hd3.setUseProxy(proxy);	
		boolean dv = hd3.deviceVendors();
		assertTrue(dv);
		JsonObject data = hd3.getReply();					
		assertEquals("\"OK\"", HD3Util.get("message", data).toString());
		assertEquals(0, HD3Util.get("status", data).getAsInt());	
		JsonArray count = hd3.getReply().get("vendor").getAsJsonArray();		
		assertTrue(count.size() > 1000 == true);
		try {
			for(String vendor : vendors) {
				assertTrue(this.jsonArrayToList(getJson(), "vendor", null, vendor));
			}			
		} catch(JSONException e) {
			e.printStackTrace();
		}
	}
		
	@Test
	public void testDeviceVendorsFail() {
		List<String> vendors = 
				new ArrayList<String>(Arrays.asList(new String[] { "Oracle", "Linux", "Azure" }));	
		hd3.deviceVendors();
		try {
			for(String vendor : vendors) {
				assertFalse(this.jsonArrayToList(getJson(), "vendor", null, vendor));
			}			
		} catch(JSONException e) {
			e.printStackTrace();
		}
	} 
	
	@Test
	public void deviceModels(boolean local, boolean proxy) {
		hd3.setUseLocal(local);
		hd3.setUseProxy(proxy);		
		boolean reply = hd3.deviceModels("Nokia");
		JsonObject data = hd3.getReply();
		JsonArray count = data.get("model").getAsJsonArray();
		assertTrue(reply);		
		assertTrue(count.size() > 700 == true);
		assertEquals("\"OK\"", HD3Util.get("message", data).toString());
		assertEquals(0, HD3Util.get("status", data).getAsInt());
	}
	
	@Test
	public void deviceView(boolean local, boolean proxy) {
		hd3.setUseLocal(local);
		hd3.setUseProxy(proxy);
		boolean reply = hd3.deviceView("Nokia", "N95");
		JsonObject data = hd3.getReply();
		assertTrue(reply);
		assertEquals("\"OK\"", HD3Util.get("message", data).toString());
		assertEquals(0, HD3Util.get("status", data).getAsInt());
	} 
	
	public void deviceWhatHas(boolean local, boolean proxy) {
		hd3.setUseLocal(local);
		hd3.setUseProxy(proxy);		
		boolean reply = hd3.deviceWhatHas("design_dimensions", "101 x 44 x 16");
		JsonObject data = hd3.getReply();		
		JsonArray jarr = HD3Util.get("devices", data).getAsJsonArray();	
		List<String> list = new ArrayList<String>();
		List<String> specs = new ArrayList<String>(Arrays.asList(new String[] { "Asus", "V80", "Spice", "S900", "Voxtel", "RX800" }));	
		for (JsonElement je : jarr) {								
			list.add(je.getAsJsonObject().get("id").toString().replaceAll("\"", ""));
			list.add(je.getAsJsonObject().get("general_vendor").toString().replaceAll("\"", ""));
			list.add(je.getAsJsonObject().get("general_model").toString().replaceAll("\"", ""));
        }
		assertTrue(reply);		
		for(String spec : specs) {
			assertEquals(true, list.indexOf(spec) > 0 ? true : false);			
		}
	}
	
	
	@Test
	public void testDeviceVendorsNokia() {				
		hd3.deviceVendors();		
		try {
			assertTrue( this.jsonArrayToList( getJson(), "vendor", null, "Nokia" ) );
		} catch (JSONException e) {			
			e.printStackTrace();
		}	
	}

	@Test
	public void testDeviceVendorsLG() {				
		hd3.deviceVendors();		
		try {
			assertTrue( this.jsonArrayToList( getJson(), "vendor", null, "LG" ) );
		} catch (JSONException e) {			
			e.printStackTrace();
		}	
	}
	
	@Test
	public void testDeviceVendorsSamsung() {				
		hd3.deviceVendors();	
		try {
			assertTrue( this.jsonArrayToList( getJson(), "vendor", null, "Samsung" ) );
		} catch (JSONException e) {			
			e.printStackTrace();
		}	
	}
	
	@Test
	public void testDeviceModelsNokia() {
		hd3.deviceModels("Nokia");		
		try {
			assertTrue( this.jsonArrayToList( getJson(), "model", null, "N95" ) );
			assertTrue( this.jsonArrayToList( getJson(), "model", null, "3310i" ) );
		} catch (JSONException e) {			
			e.printStackTrace();
		}	
	}
	
	@Test
	public void testDeviceModelsSamsung() {
		hd3.deviceModels("Samsung");
		try {			
			assertTrue( this.jsonArrayToList( getJson(), "model", null, "ATIV Odyssey" ) );
			assertTrue( this.jsonArrayToList( getJson(), "model", null, "Galaxy Y Duos Lite" ) );
		} catch (JSONException e) {			
			e.printStackTrace();
		}	
	}
	
	@Test
	public void testDeviceViewNokiaLumia() {					 
		hd3.deviceView("Nokia", "Lumia 610 NFC");		
		try {			
			assertEquals(HD3Util.get("general_platform", hd3.getReply()).getAsString(), "Windows Phone");
			assertEquals(HD3Util.get("general_model", hd3.getReply()).getAsString(), "Lumia 610 NFC");		
			assertTrue( this.jsonArrayToList( getJson(), "device", "display_other", "Gorilla Glass"));	
			assertTrue( this.jsonArrayToList( getJson(), "device", "display_other", "Multitouch"));
		} catch (JSONException e) {			
			e.printStackTrace();
		}				
	}
	
	@Test
	public void testDeviceViewAppleIPhone5S() {					 
		hd3.deviceView("Apple", "iPhone 5S");
		assertEquals(HD3Util.get("design_dimensions", hd3.getReply()).getAsString(), "123.8 x 58.6 x 7.6");
		assertEquals(HD3Util.get("display_colors", hd3.getReply()).getAsString(), "16M");			
		try {			
			assertTrue( this.jsonArrayToList(getJson(), "device", "features", "Ambient Light Sensor") );
			assertTrue( this.jsonArrayToList(getJson(), "device", "connectors", "Lightning Connector") );
		} catch (JSONException e) {			
			e.printStackTrace();
		}				
	}		
	
	@Test
	public void siteDetect(boolean local, boolean proxy) {
		hd3.setUseLocal(local);
		hd3.setUseProxy(proxy);
		for(String header : notFoundHeaders) {
			hd3.addDetectVar("user-agent", header);
			boolean reply = hd3.siteDetect();
			JsonObject data = hd3.getReply();			
			assertFalse(reply);
			assertEquals(301, HD3Util.get("status", data).getAsInt());						
		}			
		Iterator<Entry<String, Map<String, String>>> entries = map.entrySet().iterator(); 
		while(entries.hasNext()) {
			Map.Entry<String, Map<String, String>> entry = (Entry<String, Map<String, String>>) entries.next();		
			if(entry.getValue().get("user-agent") != null) {
				hd3.addDetectVar("user-agent", entry.getValue().get("user-agent"));
			}
			hd3.addDetectVar("x-wap-profile", entry.getValue().get("x-wap-profile"));
			boolean reply = hd3.siteDetect();
			JsonObject data = hd3.getReply();
			assertEquals(true, reply);
			assertEquals(0, HD3Util.get("status", data).getAsInt());
			assertEquals("\"OK\"", HD3Util.get("message", data).toString());
		}		
	}
	
	@Test
	public void testSiteDetectBrowser() {
		hd3.addDetectVar("user-agent", "Mozilla/5.0 (SymbianOS/9.2; U; Series60/3.1 NokiaN95/12.0.013; Profile/MIDP-2.0 Configuration/CLDC-1.1 ) AppleWebKit/413 (KHTML, like Gecko) Safari/413");
		hd3.siteDetect();
		try {
			JSONObject json = getJson();			
			assertEquals(json.getJSONObject("hd_specs").get("general_platform"), "Symbian");
			assertEquals(json.getJSONObject("hd_specs").get("general_platform_version"), "9.2");
			assertNotNull(json.getJSONObject("hd_specs").get("general_browser_version").toString());						
			assertTrue( this.jsonArrayToList(json, "hd_specs", "connectors", "USB") );
			assertTrue( this.jsonArrayToList(json, "hd_specs", "features", "Push to talk") );
		} catch (JSONException e) { 
			e.printStackTrace();
		}
	}

	@Test
	public void testSiteDetectXOperaMini() {
		hd3.addDetectVar("user-agent", "Opera/9.80 (Android; OperaMini/7.0.29952/28.2144; U; pt) Presto/2.8.119 Version/11.10");
		hd3.addDetectVar("x-operamini-phone", "Android #");
		hd3.addDetectVar("x-operamini-phone-ua", "Mozilla/5.0 (Linux; U;Android 2.1-update1; pt-br; U20a Build/2.1.1.A.0.6) AppleWebKit/530.17(KHTML, like Gecko) Version/4.0 Mobile Safari/530.17");
		hd3.siteDetect();
		try {
			JSONObject json = getJson();
			assertEquals(json.getJSONObject("hd_specs").get("general_platform"), "Android");
			assertEquals(json.getJSONObject("hd_specs").get("general_model"), "U20a");
			assertNotNull(json.getJSONObject("hd_specs").get("general_image").toString());						
			assertTrue( this.jsonArrayToList(json, "hd_specs", "connectors", "microUSB 2.0"));
			assertTrue( this.jsonArrayToList(json, "hd_specs", "features", "Stereo FM radio with RDS"));
		} catch (JSONException e) { 
			e.printStackTrace();
		}
	} 
	
	@Test
	public void testCloudApiCalls() {	
		this.deviceVendors(false, false);
		this.deviceModels(false, false);
		this.deviceView(false, false);				
	}
	
	@Test
	public void testCloudProxyApiCalls() {
		hd3.setUseLocal(false);
		hd3.setUseProxy(true);				
		TestCase.assertNotNull(hd3.getProxyAddress());
		TestCase.assertNotNull(hd3.getProxyPort());
		TestCase.assertNotNull(hd3.getProxyUsername());
		TestCase.assertNotNull(hd3.getProxyPassword());		
		this.deviceVendors(false, true);
		this.deviceModels(false, true);
		this.deviceView(false, true);
		this.deviceWhatHas(false, true);
		this.siteDetect(false, true);
	}

	@Test
	public void testUltimateFetchTrees() {
		hd3.setUseLocal(true);
		hd3.setUseProxy(false);
		hd3.setReadTimeout(120);		
		hd3.siteFetchArchive();
	}
	
	@Test
	public void testUltimateFetchTreesFail() {
		hd3.setUsername("bob");
		hd3.setSecret("cowcowcow");
		hd3.setSiteId("76");
		hd3.setReadTimeout(120);
		hd3.setUseLocal(true);
		hd3.setUseProxy(false);				
	}
	
	@Test
	public void testUltimateFetchSpecs() {
		hd3.setUseLocal(true);
		hd3.setUseProxy(false);		
		hd3.setReadTimeout(120);
	}
	
	@Test
	public void testUltimateFetchSpecsFail() {
		hd3.setUsername("bob");
		hd3.setSecret("cowcowcow");
		hd3.setSiteId("76");
		hd3.setReadTimeout(120);
		hd3.setUseLocal(true);
		hd3.setUseProxy(false);
	}
	
	@Test
	public void testUltimateFetchArchive() {
		String[] devices = { "Device_10.json", "Extra_546.json", "Device_46142.json", "Extra_9.json",  "Extra_102.json", "user-agent0.json", "user-agent1.json", "user-agentplatform.json", "user-agentbrowser.json", "profile0.json" };
		hd3.setUseLocal(true);
		hd3.setUseProxy(false);
		hd3.setReadTimeout(120);		
		boolean reply = hd3.siteFetchArchive();		
		TestCase.assertEquals(true, reply);
		for(String device : devices) {
			assertEquals(true, new File("files", device).exists());
		}
		try {
			String content = new Scanner(new File("files", "device_10.json"), "UTF-8").useDelimiter("\\A").next();
			assertEquals(new JsonParser().parse(content).toString(), new JsonParser().parse(this.Device_10).toString());
		} catch (FileNotFoundException e) {			
			e.printStackTrace();
		}
	}
	
	@Test
	public void testUltimateApiCalls() {
		this.deviceVendors(true, false);
		this.deviceModels(true, false);		
		this.deviceView(true, false);
		this.deviceWhatHas(true, false);
		this.siteDetect(true, false);
	}
	
	@Ignore
	private boolean jsonArrayToList(JSONObject json, String key1, String key2, String value) throws JSONException {
		JSONArray jsonArray = (key2 == null) ? (JSONArray) json.get(key1) : 
			(JSONArray) json.getJSONObject(key1).get(key2); 		
		List<String> list = new ArrayList<String>();		
		for(int i = 0; i < jsonArray.length(); i++) {
			list.add(jsonArray.getString(i));	
		}
		Collections.sort(list);	
		return ( Collections.binarySearch(list, value) > -1 ) ? true : false;		
	}
	
	@Ignore
	private JSONObject getJson() throws JSONException {		
		return (new JSONObject(hd3.getReply().toString()));
	}

}

