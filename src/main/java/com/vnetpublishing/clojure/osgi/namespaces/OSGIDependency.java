package com.vnetpublishing.clojure.osgi.namespaces;

import org.osgi.framework.Version;

import clojure.lang.Namespace;
import clojure.lang.Symbol;

public class OSGIDependency {

	protected Version version;
	protected Symbol name;
	protected Namespace namespace = null;
	protected final int hash;
	
	public OSGIDependency(String symbolicName, Version version) 
	{
		this.name = Symbol.create(symbolicName);
		this.version = version;
		int hash = symbolicName.hashCode();
		
		if (version != null) {
			 hash = (hash >> 1) + (version.toString().hashCode() >> 1);
		}
		
		this.hash = hash;
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
	
	public int hashCode() 
	{
		return hash;
	}
	
}
