package com.vnetpublishing.clojure.osgi.namespaces;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleException;
import org.osgi.framework.Version;

public class NamespaceUtil {

	//TODO: Handle quoted strings
	public static Map<String,String> parseDefinedAttributes(String in) 
	{
		
		Map<String,String> m = new HashMap<String,String>();
		if (in == null) {
			return m;
		}
		
		int from = 0;
		int start = 0;
		int tokenEqPos = 0;
		int next;
		String seg = null;
		
		while(in.indexOf(";",from) != -1) {
			start = from;
			from = in.indexOf(";",start) + 1;
			next = in.indexOf(";",from);
			
			if (next == -1) {
				seg = in.substring(from);
			} else {
				seg = in.substring(from,next);
			}
			
			if (seg.length() > 0) {
				tokenEqPos = seg.indexOf("=");
				if (tokenEqPos == -1) {
					m.put(seg, null);
				} else {
					m.put(seg.substring(0,tokenEqPos), seg.substring(tokenEqPos+1));
				}
			}
		}
		
		return m;
	}
	
	public static String parseDefinedSymbol(String in) 
	{
		int end = in.indexOf(";");
		return end == -1 ? in : in.substring(0,end);
	}

	public static OSGIDependency findImport(String nsimport, List<OSGIDependency> avail) 
	{
		Map<String,String> attrs = parseDefinedAttributes(nsimport);
		
		String symName = parseDefinedSymbol(nsimport);
		
		List<Version> hardVersions = new ArrayList<Version>();
		Version gteVersion = null;
		Version ltVersion = null;
		
		String rawVer = attrs.get("version");
		if (null != rawVer) {
			List<String> rules = Arrays.asList(rawVer.split(","));
			
			for(String rule : rules) {
				if (rule.startsWith("[")) {
					gteVersion = new Version(rule.substring(1));
				}  else if (rule.endsWith(")")) {
					ltVersion = new Version(rule.substring(0,rule.length() - 1));
				} else {
					hardVersions.add(new Version(rule));
				}
				
			}
			
		}
		
		OSGIDependency maybe = null;
		
		boolean norules = gteVersion == null &&  ltVersion == null && hardVersions.size() < 1;
		
		for(OSGIDependency dep : avail) {
			if (dep.name.getName().equals(symName)) {
				Version check = dep.getVersion();
				if (norules) {
					if (maybe == null) {
						maybe = dep;
					} else {
						if (check != null) {
							if (maybe.getVersion().compareTo(dep.getVersion()) > 0) {
								maybe = dep;
							}
						}
					}
				}
				
				//  Dep must have a version if rules provided, above deals with no rules
				if (check != null) {
					if (hardVersions != null) {
						for(Version v: hardVersions) {
							if (check.compareTo(v) == 0) {
								return dep; // equivalence rules!
							}
						}
					}
				
					int gteCompare = check.compareTo(gteVersion);
					if (gteCompare >= 0) {
						if (gteVersion == null) {
							return dep; // maybe???
						} else {
							if (check.compareTo(gteVersion) < 0) {
								return dep; // maybe???
							}
						}
					}
				}
			}
		}
		return maybe;
	}

	public static String bundleStateName(int state) 
	{
		switch(state) {
			case Bundle.INSTALLED:
				return "INSTALLED";
			case Bundle.ACTIVE:
				return "ACTIVE";
			case Bundle.UNINSTALLED:
				return "UNINSTALLED";
			case Bundle.RESOLVED:
				return "RESOLVED";
			case Bundle.STARTING:
				return "STARTING";
			case Bundle.STOPPING:
				return "STOPPING";
		}
		return String.format("Unknown state (%d)",state);
	}
	
	public static String bundleEventTypeName(int event_type)
	{
		switch(event_type) {
			case BundleEvent.STARTING:
				return "STARTING";
			case BundleEvent.INSTALLED:
				return "INSTALLED";
			case BundleEvent.RESOLVED:
				return "RESOLVED";
			case BundleEvent.STARTED:
				return "STARTED";
			case BundleEvent.STOPPED:
				return "STOPPED";
			case BundleEvent.STOPPING:
				return "STOPPING";
			case BundleEvent.UNINSTALLED:
				return "UNINSTALLED";
			case BundleEvent.UNRESOLVED:
				return "UNRESOLVED";
			case BundleEvent.UPDATED:
				return "UPDATED";
		}
		return String.format("Unknown BundleEvent Type (%d)",event_type);
	}
	
	public static void startBundle(Bundle bundle) throws BundleException 
	{
		// Bundle listener will handle the callStart on STARTED
		bundle.start();
	}
	
	public static void stopBundle(Bundle bundle) throws BundleException 
	{
		// Bundle listener will handle the callStart on STOPPING
		bundle.stop();
	}
	
}
