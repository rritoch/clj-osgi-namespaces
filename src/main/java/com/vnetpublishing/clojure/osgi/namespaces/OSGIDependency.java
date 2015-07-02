package com.vnetpublishing.clojure.osgi.namespaces;

import org.osgi.framework.Version;

import clojure.lang.Namespace;
import clojure.lang.Symbol;

public class OSGIDependency {

	protected Version version;
	protected Symbol name;
	protected Namespace namespace = null;
	
	public OSGIDependency(String symbolicName, Version version) 
	{
		this.name = Symbol.create(symbolicName);
		this.version = version;
	}
	
	public Symbol getName() 
	{
		return name;
	}

	public Namespace getNamespace() 
	{
		return namespace;
	}
	
	public Version getVersion() 
	{
		return version;
	}
	
	public void setNamespace(Namespace ns) 
	{
		namespace = ns;
	}
	
}
