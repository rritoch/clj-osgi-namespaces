package com.vnetpublishing.clojure.osgi.namespaces;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class ClojureOSGIActivator implements BundleActivator {
	
	private static final Logger logger = Logger.getLogger(ClojureOSGIActivator.class.getName());

	@Override
	public void start(BundleContext context) throws Exception 
	{
		ClassLoader ccl = Thread.currentThread().getContextClassLoader();
		logger.info(String.format("Starting ClojureOSGIActivator: contextClassloader = %s with classname %s",ccl.toString(),ccl.getClass().getName()));
		List<String> exports = new ArrayList<String>();
		exports.add("clojure.core");
		DeligatingNamespaceRegistry.startFramework(context, exports);
		logger.info("ClojureOSGIActivator: Started!");
	}

	@Override
	public void stop(BundleContext context) throws Exception 
	{
		// TODO Auto-generated method stub
		
	}

}
