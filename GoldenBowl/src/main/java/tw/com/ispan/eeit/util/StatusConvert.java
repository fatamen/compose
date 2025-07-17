package tw.com.ispan.eeit.util;

public class StatusConvert {

	public static String toDTO(String status) {
		status =status.toLowerCase();
		if(status.equals("pending")){
			return "待確認";
		}
		if(status.equals("perparing")) {
			return "準備中";
		}
		if(status.equals("cancelled")) {
			return "已取消";
		}
		if(status.equals("completed")) {
			return "已完成"; 
		}
		
		return status;
	}
	
	
	
	
}
