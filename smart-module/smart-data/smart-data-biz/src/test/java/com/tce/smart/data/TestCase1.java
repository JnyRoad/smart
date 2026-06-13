package com.tce.smart.data;

import com.tce.smart.data.webservice.newservice.RequestService;
import com.tce.smart.data.webservice.newservice.RequestServicePortType;

import java.net.URL;

/**
 * @author fushiping
 * @date 2020/7/30 0030 19:30
 **/
public class TestCase1 {
	public static void main(String[] args) throws Exception{
// write your code here
		RequestServicePortType requestServicePortType = new RequestService(new URL("http://10.0.20.69/services/RequestService?wsdl")).getRequestServiceHttpPort();

		boolean test = requestServicePortType.nextNodeByReject(123, 123, "test");

		System.out.println(test);
	}
}
