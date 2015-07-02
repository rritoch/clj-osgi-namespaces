package com.vnetpublishing.clojure.osgi.namespaces;

import java.util.ArrayList;
import java.util.List;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class ClojureOSGIActivator implements BundleActivator {

	@Override
	public void start(BundleContext context) throws Exception 
	{
		List<String> exports = new ArrayList<String>();
		exports.add("clojure.core");
		DeligatingNamespaceRegistry.startFramework(context, exports);
	}

	@Override
	public void stop(BundleContext context) throws Exception 
	{
		// TODO Auto-generated method stub
		
	}

}
