/**
 * WorkflowServicePortType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.tce.smart.data.webservice.client;

public interface WorkflowServicePortType extends java.rmi.Remote {
    boolean deleteRequest(int in0, int in1) throws java.rmi.RemoteException;
    java.lang.String submitWorkflowRequest(com.tce.smart.data.webservice.databinding.WorkflowRequestInfo in0, int in1, int in2, java.lang.String in3, java.lang.String in4) throws java.rmi.RemoteException;
    int getToDoWorkflowRequestCount(int in0, java.lang.String[] in1) throws java.rmi.RemoteException;
    java.lang.String doCreateWorkflowRequest(com.tce.smart.data.webservice.databinding.WorkflowRequestInfo in0, int in1) throws java.rmi.RemoteException;
    com.tce.smart.data.webservice.databinding.WorkflowRequestInfo getCreateWorkflowRequestInfo(int in0, int in1) throws java.rmi.RemoteException;
    com.tce.smart.data.webservice.databinding.WorkflowRequestInfo[] getAllWorkflowRequestList(int in0, int in1, int in2, int in3, java.lang.String[] in4) throws java.rmi.RemoteException;
    com.tce.smart.data.webservice.databinding.WorkflowRequestInfo[] getMyWorkflowRequestList(int in0, int in1, int in2, int in3, java.lang.String[] in4) throws java.rmi.RemoteException;
    int getProcessedWorkflowRequestCount(int in0, java.lang.String[] in1) throws java.rmi.RemoteException;
    com.tce.smart.data.webservice.databinding.WorkflowRequestInfo getWorkflowRequest(int in0, int in1, int in2) throws java.rmi.RemoteException;
    java.lang.String getLeaveDays(java.lang.String in0, java.lang.String in1, java.lang.String in2, java.lang.String in3, java.lang.String in4) throws java.rmi.RemoteException;
    com.tce.smart.data.webservice.databinding.WorkflowRequestInfo getWorkflowRequest4Split(int in0, int in1, int in2, int in3) throws java.rmi.RemoteException;
    com.tce.smart.data.webservice.databinding.WorkflowRequestInfo[] getHendledWorkflowRequestList(int in0, int in1, int in2, int in3, java.lang.String[] in4) throws java.rmi.RemoteException;
    int getCreateWorkflowCount(int in0, int in1, java.lang.String[] in2) throws java.rmi.RemoteException;
    com.tce.smart.data.webservice.databinding.WorkflowRequestInfo[] getToDoWorkflowRequestList(int in0, int in1, int in2, int in3, java.lang.String[] in4) throws java.rmi.RemoteException;
    java.lang.String[] getWorkflowNewFlag(java.lang.String[] in0, java.lang.String in1) throws java.rmi.RemoteException;
    com.tce.smart.data.webservice.databinding.WorkflowRequestInfo[] getCCWorkflowRequestList(int in0, int in1, int in2, int in3, java.lang.String[] in4) throws java.rmi.RemoteException;
    com.tce.smart.data.webservice.databinding.WorkflowBaseInfo[] getCreateWorkflowList(int in0, int in1, int in2, int in3, int in4, java.lang.String[] in5) throws java.rmi.RemoteException;
    com.tce.smart.data.webservice.databinding.WorkflowRequestLog[] getWorkflowRequestLogs(java.lang.String in0, java.lang.String in1, int in2, int in3, int in4) throws java.rmi.RemoteException;
    int getCCWorkflowRequestCount(int in0, java.lang.String[] in1) throws java.rmi.RemoteException;
    int getAllWorkflowRequestCount(int in0, java.lang.String[] in1) throws java.rmi.RemoteException;
    int getMyWorkflowRequestCount(int in0, java.lang.String[] in1) throws java.rmi.RemoteException;
    com.tce.smart.data.webservice.databinding.WorkflowRequestInfo[] getProcessedWorkflowRequestList(int in0, int in1, int in2, int in3, java.lang.String[] in4) throws java.rmi.RemoteException;
    int getCreateWorkflowTypeCount(int in0, java.lang.String[] in1) throws java.rmi.RemoteException;
    java.lang.String forwardWorkflowRequest(int in0, java.lang.String in1, java.lang.String in2, int in3, java.lang.String in4) throws java.rmi.RemoteException;
    void writeWorkflowReadFlag(java.lang.String in0, java.lang.String in1) throws java.rmi.RemoteException;
    int getHendledWorkflowRequestCount(int in0, java.lang.String[] in1) throws java.rmi.RemoteException;
    com.tce.smart.data.webservice.databinding.WorkflowBaseInfo[] getCreateWorkflowTypeList(int in0, int in1, int in2, int in3, java.lang.String[] in4) throws java.rmi.RemoteException;
}
