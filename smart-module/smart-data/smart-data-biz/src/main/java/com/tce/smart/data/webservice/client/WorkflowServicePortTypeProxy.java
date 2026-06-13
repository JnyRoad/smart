package com.tce.smart.data.webservice.client;

import org.springframework.stereotype.Component;

@Component
public class WorkflowServicePortTypeProxy implements com.tce.smart.data.webservice.client.WorkflowServicePortType {
  private String _endpoint = null;
  private com.tce.smart.data.webservice.client.WorkflowServicePortType workflowServicePortType = null;

  public WorkflowServicePortTypeProxy() {
    _initWorkflowServicePortTypeProxy();
  }

  public WorkflowServicePortTypeProxy(String endpoint) {
    _endpoint = endpoint;
    _initWorkflowServicePortTypeProxy();
  }

  private void _initWorkflowServicePortTypeProxy() {
    try {
      workflowServicePortType = (new com.tce.smart.data.webservice.client.WorkflowServiceLocator()).getWorkflowServiceHttpPort();
      if (workflowServicePortType != null) {
        if (_endpoint != null)
          ((javax.xml.rpc.Stub)workflowServicePortType)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
        else
          _endpoint = (String)((javax.xml.rpc.Stub)workflowServicePortType)._getProperty("javax.xml.rpc.service.endpoint.address");
      }

    }
    catch (javax.xml.rpc.ServiceException serviceException) {}
  }

  public String getEndpoint() {
    return _endpoint;
  }

  public void setEndpoint(String endpoint) {
    _endpoint = endpoint;
    if (workflowServicePortType != null)
      ((javax.xml.rpc.Stub)workflowServicePortType)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);

  }

  public com.tce.smart.data.webservice.client.WorkflowServicePortType getWorkflowServicePortType() {
    if (workflowServicePortType == null)
      _initWorkflowServicePortTypeProxy();
    return workflowServicePortType;
  }

  public boolean deleteRequest(int in0, int in1) throws java.rmi.RemoteException{
    if (workflowServicePortType == null)
      _initWorkflowServicePortTypeProxy();
    return workflowServicePortType.deleteRequest(in0, in1);
  }

  public java.lang.String submitWorkflowRequest(com.tce.smart.data.webservice.databinding.WorkflowRequestInfo in0, int in1, int in2, java.lang.String in3, java.lang.String in4) throws java.rmi.RemoteException{
    if (workflowServicePortType == null)
      _initWorkflowServicePortTypeProxy();
    return workflowServicePortType.submitWorkflowRequest(in0, in1, in2, in3, in4);
  }

  public int getToDoWorkflowRequestCount(int in0, java.lang.String[] in1) throws java.rmi.RemoteException{
    if (workflowServicePortType == null)
      _initWorkflowServicePortTypeProxy();
    return workflowServicePortType.getToDoWorkflowRequestCount(in0, in1);
  }

  public java.lang.String doCreateWorkflowRequest(com.tce.smart.data.webservice.databinding.WorkflowRequestInfo in0, int in1) throws java.rmi.RemoteException{
    if (workflowServicePortType == null)
      _initWorkflowServicePortTypeProxy();
    return workflowServicePortType.doCreateWorkflowRequest(in0, in1);
  }

  public com.tce.smart.data.webservice.databinding.WorkflowRequestInfo getCreateWorkflowRequestInfo(int in0, int in1) throws java.rmi.RemoteException{
    if (workflowServicePortType == null)
      _initWorkflowServicePortTypeProxy();
    return workflowServicePortType.getCreateWorkflowRequestInfo(in0, in1);
  }

  public com.tce.smart.data.webservice.databinding.WorkflowRequestInfo[] getAllWorkflowRequestList(int in0, int in1, int in2, int in3, java.lang.String[] in4) throws java.rmi.RemoteException{
    if (workflowServicePortType == null)
      _initWorkflowServicePortTypeProxy();
    return workflowServicePortType.getAllWorkflowRequestList(in0, in1, in2, in3, in4);
  }

  public com.tce.smart.data.webservice.databinding.WorkflowRequestInfo[] getMyWorkflowRequestList(int in0, int in1, int in2, int in3, java.lang.String[] in4) throws java.rmi.RemoteException{
    if (workflowServicePortType == null)
      _initWorkflowServicePortTypeProxy();
    return workflowServicePortType.getMyWorkflowRequestList(in0, in1, in2, in3, in4);
  }

  public int getProcessedWorkflowRequestCount(int in0, java.lang.String[] in1) throws java.rmi.RemoteException{
    if (workflowServicePortType == null)
      _initWorkflowServicePortTypeProxy();
    return workflowServicePortType.getProcessedWorkflowRequestCount(in0, in1);
  }

  public com.tce.smart.data.webservice.databinding.WorkflowRequestInfo getWorkflowRequest(int in0, int in1, int in2) throws java.rmi.RemoteException{
    if (workflowServicePortType == null)
      _initWorkflowServicePortTypeProxy();
    return workflowServicePortType.getWorkflowRequest(in0, in1, in2);
  }

  public java.lang.String getLeaveDays(java.lang.String in0, java.lang.String in1, java.lang.String in2, java.lang.String in3, java.lang.String in4) throws java.rmi.RemoteException{
    if (workflowServicePortType == null)
      _initWorkflowServicePortTypeProxy();
    return workflowServicePortType.getLeaveDays(in0, in1, in2, in3, in4);
  }

  public com.tce.smart.data.webservice.databinding.WorkflowRequestInfo getWorkflowRequest4Split(int in0, int in1, int in2, int in3) throws java.rmi.RemoteException{
    if (workflowServicePortType == null)
      _initWorkflowServicePortTypeProxy();
    return workflowServicePortType.getWorkflowRequest4Split(in0, in1, in2, in3);
  }

  public com.tce.smart.data.webservice.databinding.WorkflowRequestInfo[] getHendledWorkflowRequestList(int in0, int in1, int in2, int in3, java.lang.String[] in4) throws java.rmi.RemoteException{
    if (workflowServicePortType == null)
      _initWorkflowServicePortTypeProxy();
    return workflowServicePortType.getHendledWorkflowRequestList(in0, in1, in2, in3, in4);
  }

  public int getCreateWorkflowCount(int in0, int in1, java.lang.String[] in2) throws java.rmi.RemoteException{
    if (workflowServicePortType == null)
      _initWorkflowServicePortTypeProxy();
    return workflowServicePortType.getCreateWorkflowCount(in0, in1, in2);
  }

  public com.tce.smart.data.webservice.databinding.WorkflowRequestInfo[] getToDoWorkflowRequestList(int in0, int in1, int in2, int in3, java.lang.String[] in4) throws java.rmi.RemoteException{
    if (workflowServicePortType == null)
      _initWorkflowServicePortTypeProxy();
    return workflowServicePortType.getToDoWorkflowRequestList(in0, in1, in2, in3, in4);
  }

  public java.lang.String[] getWorkflowNewFlag(java.lang.String[] in0, java.lang.String in1) throws java.rmi.RemoteException{
    if (workflowServicePortType == null)
      _initWorkflowServicePortTypeProxy();
    return workflowServicePortType.getWorkflowNewFlag(in0, in1);
  }

  public com.tce.smart.data.webservice.databinding.WorkflowRequestInfo[] getCCWorkflowRequestList(int in0, int in1, int in2, int in3, java.lang.String[] in4) throws java.rmi.RemoteException{
    if (workflowServicePortType == null)
      _initWorkflowServicePortTypeProxy();
    return workflowServicePortType.getCCWorkflowRequestList(in0, in1, in2, in3, in4);
  }

  public com.tce.smart.data.webservice.databinding.WorkflowBaseInfo[] getCreateWorkflowList(int in0, int in1, int in2, int in3, int in4, java.lang.String[] in5) throws java.rmi.RemoteException{
    if (workflowServicePortType == null)
      _initWorkflowServicePortTypeProxy();
    return workflowServicePortType.getCreateWorkflowList(in0, in1, in2, in3, in4, in5);
  }

  public com.tce.smart.data.webservice.databinding.WorkflowRequestLog[] getWorkflowRequestLogs(java.lang.String in0, java.lang.String in1, int in2, int in3, int in4) throws java.rmi.RemoteException{
    if (workflowServicePortType == null)
      _initWorkflowServicePortTypeProxy();
    return workflowServicePortType.getWorkflowRequestLogs(in0, in1, in2, in3, in4);
  }

  public int getCCWorkflowRequestCount(int in0, java.lang.String[] in1) throws java.rmi.RemoteException{
    if (workflowServicePortType == null)
      _initWorkflowServicePortTypeProxy();
    return workflowServicePortType.getCCWorkflowRequestCount(in0, in1);
  }

  public int getAllWorkflowRequestCount(int in0, java.lang.String[] in1) throws java.rmi.RemoteException{
    if (workflowServicePortType == null)
      _initWorkflowServicePortTypeProxy();
    return workflowServicePortType.getAllWorkflowRequestCount(in0, in1);
  }

  public int getMyWorkflowRequestCount(int in0, java.lang.String[] in1) throws java.rmi.RemoteException{
    if (workflowServicePortType == null)
      _initWorkflowServicePortTypeProxy();
    return workflowServicePortType.getMyWorkflowRequestCount(in0, in1);
  }

  public com.tce.smart.data.webservice.databinding.WorkflowRequestInfo[] getProcessedWorkflowRequestList(int in0, int in1, int in2, int in3, java.lang.String[] in4) throws java.rmi.RemoteException{
    if (workflowServicePortType == null)
      _initWorkflowServicePortTypeProxy();
    return workflowServicePortType.getProcessedWorkflowRequestList(in0, in1, in2, in3, in4);
  }

  public int getCreateWorkflowTypeCount(int in0, java.lang.String[] in1) throws java.rmi.RemoteException{
    if (workflowServicePortType == null)
      _initWorkflowServicePortTypeProxy();
    return workflowServicePortType.getCreateWorkflowTypeCount(in0, in1);
  }

  public java.lang.String forwardWorkflowRequest(int in0, java.lang.String in1, java.lang.String in2, int in3, java.lang.String in4) throws java.rmi.RemoteException{
    if (workflowServicePortType == null)
      _initWorkflowServicePortTypeProxy();
    return workflowServicePortType.forwardWorkflowRequest(in0, in1, in2, in3, in4);
  }

  public void writeWorkflowReadFlag(java.lang.String in0, java.lang.String in1) throws java.rmi.RemoteException{
    if (workflowServicePortType == null)
      _initWorkflowServicePortTypeProxy();
    workflowServicePortType.writeWorkflowReadFlag(in0, in1);
  }

  public int getHendledWorkflowRequestCount(int in0, java.lang.String[] in1) throws java.rmi.RemoteException{
    if (workflowServicePortType == null)
      _initWorkflowServicePortTypeProxy();
    return workflowServicePortType.getHendledWorkflowRequestCount(in0, in1);
  }

  public com.tce.smart.data.webservice.databinding.WorkflowBaseInfo[] getCreateWorkflowTypeList(int in0, int in1, int in2, int in3, java.lang.String[] in4) throws java.rmi.RemoteException{
    if (workflowServicePortType == null)
      _initWorkflowServicePortTypeProxy();
    return workflowServicePortType.getCreateWorkflowTypeList(in0, in1, in2, in3, in4);
  }


}