package nslookup_2.shared;

import java.util.ArrayList;

public class DNSQuery {
	private String domain;
	ArrayList<String> recordTypes = new ArrayList<String>();
	
	public DNSQuery(String domain, ArrayList<String> recordTypes) {
		this.domain = domain;
		this.recordTypes = recordTypes;
	}
	
	public String getDomain() { return domain; }
	public ArrayList<String> getRecordTypes() { return recordTypes; }
}
