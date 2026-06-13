/**
 * WorkflowService.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.tce.smart.data.webservice.client;

public interface WorkflowService extends javax.xml.rpc.Service {
    java.lang.String getWorkflowServiceHttpPortAddress();

    com.tce.smart.data.webservice.client.WorkflowServicePortType getWorkflowServiceHttpPort() throws javax.xml.rpc.ServiceException;

    com.tce.smart.data.webservice.client.WorkflowServicePortType getWorkflowServiceHttpPort(java.net.URL portAddress) throws javax.xml.rpc.ServiceException;
}
