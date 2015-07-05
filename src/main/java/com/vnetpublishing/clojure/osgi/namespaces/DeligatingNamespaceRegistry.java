package com.vnetpublishing.clojure.osgi.namespaces;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import clojure.java.api.Clojure;
import clojure.lang.IFn;
import clojure.lang.IPersistentMap;
import clojure.lang.Namespace;
import clojure.lang.RT;
import clojure.lang.Symbol;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.osgi.framework.Version;
import org.osgi.framework.wiring.BundleWiring;

public class DeligatingNamespaceRegistry extends ConcurrentHashMap<Symbol, Namespace> 
	implements BundleListener
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -5269309431626927020L;

	protected static boolean active = false;
	protected static Object origNamespaces = null;
	
	private static final Logger logger = Logger.getLogger(DeligatingNamespaceRegistry.class.getName());
	
	protected static DeligatingNamespaceRegistry inst = null;
	
	
	protected ConcurrentHashMap<ClassLoader,ConcurrentHashMap<Symbol, Namespace>> registrations = new ConcurrentHashMap<ClassLoader,ConcurrentHashMap<Symbol, Namespace>>();
	
	protected Map<OSGIDependency,Namespace>  provided = new HashMap<OSGIDependency,Namespace>();
	
	protected Map<ClassLoader,List<OSGIDependency>> providers = new HashMap<ClassLoader,List<OSGIDependency>>();
	
	
	private ThreadLocal<ConcurrentHashMap<Symbol, Namespace>> currentRegistry = new ThreadLocal<ConcurrentHashMap<Symbol, Namespace>>();
	
	protected  ConcurrentHashMap<Symbol, Namespace> getActiveNamespaces() 
	{
		
		if (currentRegistry.get() != null) {
			return currentRegistry.get();
		}
		
		ConcurrentHashMap<Symbol, Namespace> ret = null; 
		ClassLoader scl = ClassLoader.getSystemClassLoader();
		//ClassLoader cl = Thread.currentThread().getContextClassLoader();
		ClassLoader cl = RT.baseLoader();
		while (ret == null && cl != null && cl != scl) {
			ret = registrations.get(cl);
			cl = cl.getParent();
		}
		if (ret == null) {
			if (cl == ClassLoader.getSystemClassLoader()) {
				ret = registrations.get(cl);
			}
			if (ret == null) {
				ret = new ConcurrentHashMap<Symbol, Namespace>();
			}
		}
		return ret;
	}
	
	@Override 
	public void clear() {
		getActiveNamespaces().clear();
	}
	
	@Override
	public boolean contains(Object value) {
		return getActiveNamespaces().contains(value);
	}
	
	@Override
	public boolean containsKey(Object value) {
		return getActiveNamespaces().containsKey(value);
	}
	
	@Override
	public boolean containsValue(Object value) {
		return getActiveNamespaces().containsValue(value);
	}
	
	
	@Override
	public Enumeration<Namespace> elements() 
	{
		return getActiveNamespaces().elements();
	}
	
	@Override
	public Set<Entry<Symbol,Namespace>> entrySet() 
	{
		return getActiveNamespaces().entrySet();
	}	
	
	
	@Override 
	public Namespace get(Object key) 
	{
		return getActiveNamespaces().get(key);
	}
	
	@Override
	public boolean isEmpty() 
	{
		return getActiveNamespaces().isEmpty();
	}
	
	@Override
	public Enumeration<Symbol> keys() 
	{
		
		return getActiveNamespaces().keys();
	}
	
	@Override
	public KeySetView<Symbol,Namespace> keySet() {
		return getActiveNamespaces().keySet();
	}

	@Override
	public Namespace put(Symbol key,Namespace value) {
		return getActiveNamespaces().put(key, value);
	}
	
	@Override
	public void putAll(Map<? extends Symbol, ? extends Namespace> m) 
	{
		getActiveNamespaces().putAll(m);
	}
	
	@Override
	public Namespace putIfAbsent(Symbol k, Namespace v) 
	{
		return getActiveNamespaces().putIfAbsent(k, v);
	}
	
	@Override
	public Namespace remove(Object key) {
		return  getActiveNamespaces().remove(key);
	}
	
	@Override
	public boolean remove(Object key, Object value) 
	{
		return getActiveNamespaces().remove(key, value);
	}
	
	@Override
	public Namespace replace(Symbol k, Namespace v) 
	{
		return getActiveNamespaces().replace(k, v);
	}
	
	
	@Override
	public boolean replace(Symbol k, Namespace oldValue, Namespace newValue) 
	{
		return getActiveNamespaces().replace(k, oldValue,newValue);
	}
	
	@Override
	public int size() 
	{
		return getActiveNamespaces().size();
	}
	
	@Override
	public Collection<Namespace> values() {
		return getActiveNamespaces().values();
	}
	
	protected OSGIDependency findImport(String nsimport) {
		List<OSGIDependency> avail = new ArrayList<OSGIDependency>();
		avail.addAll(provided.keySet());
		return NamespaceUtil.findImport(nsimport,avail);
	}
	
	protected synchronized void register(ClassLoader cl, List<String> exports, List<String> imports) {
		
		ConcurrentHashMap<Symbol, Namespace> reg = registrations.get(cl);
		if (reg == null) {
			reg = new ConcurrentHashMap<Symbol, Namespace>();
		}
		
		List<OSGIDependency> myExports = new ArrayList<OSGIDependency>();
		List<OSGIDependency> myImports = new ArrayList<OSGIDependency>();
		
		// Populate export list
		
		if (exports != null) {
			for (String export : exports) {
				String versionStr = NamespaceUtil.parseDefinedAttributes(export).get("version");
				if (versionStr == null || versionStr.length() < 1) {
					myExports.add(new OSGIDependency(NamespaceUtil.parseDefinedSymbol(export),null));
				} else {
					myExports.add(new OSGIDependency(NamespaceUtil.parseDefinedSymbol(export),new Version(versionStr)));
				}
			}
		}
		
		// Grab all imports, exception if import is not an export
		
		if (imports != null) {
			for (String nsimport : imports) {
				OSGIDependency i = findImport(nsimport);
			
				if (i == null) {
					i = NamespaceUtil.findImport(nsimport,myExports);
				}
			
				if (i == null) {
					throw new RuntimeException("Missing import");
				}
			
				myImports.add(i);
			}
		}
		
		// Generate all exports, they should exist eventually
		
		currentRegistry.set(reg); // Temporarily activate runtime
		for (OSGIDependency export : myExports) {
			Namespace ns = Namespace.findOrCreate(export.getName());
			reg.put(ns.getName(),ns);
		}
		currentRegistry.set(null);
		
		// Process imports
		
		for (OSGIDependency myImport : myImports) {
			Namespace ns = reg.get(myImport.getName());
			Namespace nsin = myImport.getNamespace();
			if (ns == null) {
				reg.put(myImport.getName(), nsin);
			} else {
				// Pass mappings
				IPersistentMap m = ns.getMappings();
				Iterator i = m.iterator();
				while(i.hasNext()) {
					Symbol s = (Symbol)i.next();
				}
				
				// TODO: Process aliases?
			}
		}
		
		// Finally register exports
		for (OSGIDependency export : myExports) {
			provided.put(export, reg.get(export.getName()));
		}
		
		providers.put(cl,myExports);
		
		registrations.put(cl,reg);
	}
	
	protected void unregister(ClassLoader cl) 
	{
		ConcurrentHashMap<Symbol, Namespace> reg = registrations.get(cl);
		
		if (reg == null) {
			return; // Nothing to do!
		}
		
		List<OSGIDependency> pExports = providers.get(cl);
		
		if (pExports != null) {
			for (OSGIDependency export : pExports) {
				provided.remove(export, reg.get(export.getName()));
			}
			providers.remove(cl);
		}
		
		registrations.remove(cl);
	}
	
	public static boolean startFramework(BundleContext bundleContext, List<String> exports) 
	{
		if (isActive()) {
			logger.warning("Refusing to start Clojure OSGI framework, it is already active");
			return false;
		}
		
		active = true;
		DeligatingNamespaceRegistry r = new DeligatingNamespaceRegistry();
		r.register(ClassLoader.getSystemClassLoader(), exports, null);
		
		Field namespaces;
		try {
			namespaces = Namespace.class.getDeclaredField("namespaces");
			namespaces.setAccessible(true);
			Field modifiersField = Field.class.getDeclaredField("modifiers");
			modifiersField.setAccessible(true);
			modifiersField.setInt(namespaces, namespaces.getModifiers() & ~Modifier.FINAL);
			origNamespaces = namespaces.get(null);
			namespaces.set(null, r);
		} catch (NoSuchFieldException e) {
			active = false;
			throw new RuntimeException(e);
		} catch (SecurityException e) {
			active = false;
			throw new RuntimeException(e);
		} catch (IllegalArgumentException e) {
			active = false;
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			active = false;
			throw new RuntimeException(e);
		}
		
		r.start(bundleContext);
		inst = r;
		return true;
	}

	public static boolean stopFramework(BundleContext bundleContext) 
	{
		if (!isActive() || origNamespaces == null) {
			return false;
		}

		//OSGI Framework SHOULD stop dependencies before us, so we shouldn't need to stop bundles 
		
		Field namespaces;
		try {
			namespaces = Namespace.class.getDeclaredField("namespaces");
			namespaces.setAccessible(true);
			Field modifiersField = Field.class.getDeclaredField("modifiers");
			modifiersField.setAccessible(true);
			modifiersField.setInt(namespaces, namespaces.getModifiers() & ~Modifier.FINAL);
			namespaces.set(null, origNamespaces);
		} catch (NoSuchFieldException e) {
			active = false;
			throw new RuntimeException(e);
		} catch (SecurityException e) {
			active = false;
			throw new RuntimeException(e);
		} catch (IllegalArgumentException e) {
			active = false;
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			active = false;
			throw new RuntimeException(e);
		}

		origNamespaces = null;
		active = false;
		inst = null;
		return true;
	}
	
	protected void start(BundleContext bundleContext) 
	{
		bundleContext.addBundleListener(this);
		List<Bundle> bundles = Arrays.asList(bundleContext.getBundles());
		for(Bundle bundle : bundles) {
			int bstate = bundle.getState();
			if (Bundle.RESOLVED == bstate) { 
				init(bundle);
			}
		}
	}
	
	public boolean isClojureBundle(Bundle bundle) 
	{
		logger.fine(String.format("isClojureBundle(%s)",bundle.getSymbolicName()));
		Dictionary<String,String> headers = bundle.getHeaders();
		
		String clojure_imports = headers.get("Clojure-Imports"); 
		logger.fine(String.format("Clojure-Imports: %s",String.valueOf(clojure_imports)));
		String clojure_exports = headers.get("Clojure-Exports");
		logger.fine(String.format("Clojure-Exports: %s",String.valueOf(clojure_exports)));		
		String clojure_enable = headers.get("Clojure-Enable");
		logger.fine(String.format("Clojure-Enable: %s",String.valueOf(clojure_enable)));
		
		String clojure_activator_namespace = headers.get("Clojure-Activator-Namespace");
		logger.fine(String.format("Clojure-Activator-Namespace: %s",String.valueOf(clojure_activator_namespace)));
		
		if (null != clojure_imports) {
			return true;
		}

		if (null != clojure_exports) {
			return true;
		}

		if (null != clojure_activator_namespace) {
			return true;
		}
		
		if (null != clojure_enable) {
			if ("true".equals(clojure_enable.toLowerCase())) {
				return true;
			}
		}
		return false;
	}
	
	protected void init(Bundle bundle) 
	{
		
		if (registrations == null) return; // this shouldn't really happen
		int state = bundle.getState();
		
		logger.info(String.format("Initializing bundle %s has state %s",bundle.getSymbolicName(),NamespaceUtil.bundleStateName(state)));
		if (state == Bundle.STARTING || state == Bundle.INSTALLED || state == Bundle.RESOLVED) {
			
			if (isClojureBundle(bundle)) {
			
				ClassLoader cl = bundle.adapt(BundleWiring.class).getClassLoader();
				if (registrations.get(cl) != null) {
					logger.warning(String.format("Classloader for %s has already been registered",bundle.getSymbolicName()));
					return; // Dejavu!
				}
			
				Dictionary<String,String> headers = bundle.getHeaders();
				
				String importsRaw = headers.get("Clojure-Imports");
				List<String> imports = null;
				if (importsRaw != null) {
					imports =  Arrays.asList(importsRaw.split(","));
				}
				
				String exportsRaw = headers.get("Clojure-Exports");
				List<String> exports = null;
				if (exportsRaw != null) {
					exports = Arrays.asList(exportsRaw.split(","));
				}
				
				register(cl,exports,imports);
				logger.info(String.format("Clojure bundle %s has been registered",bundle.getSymbolicName()));
			}
		} else {
			if (isClojureBundle(bundle)) {
				logger.warning(String.format("Clojure bundle %s missed it's chance to be registered",bundle.getSymbolicName()));
			}
		}
	}
	
	protected void uninit(Bundle bundle) 
	{
		if (registrations == null) return; // this shouldn't really happen
		int state = bundle.getState();
		
		logger.info(String.format("Initializing bundle %s has state %s",bundle.getSymbolicName(),NamespaceUtil.bundleStateName(state)));
		if (state == Bundle.RESOLVED || state == Bundle.ACTIVE) {
			
			if (isClojureBundle(bundle)) {
			
				ClassLoader cl = bundle.adapt(BundleWiring.class).getClassLoader();
				if (registrations.get(cl) == null) {
					logger.warning(String.format("Classloader for %s isn't registered",bundle.getSymbolicName()));
					return; // Dejavu!
				}
			
				unregister(cl);
				logger.info(String.format("Clojure bundle %s has been unregistered",bundle.getSymbolicName()));
			}
		} else {
			if (isClojureBundle(bundle)) {
				logger.warning(String.format("Clojure bundle %s missed it's chance to be registered",bundle.getSymbolicName()));
			}
		}
	}
	
	protected void callStart(Bundle bundle) 
	{
		if (isClojureBundle(bundle)) {
			Dictionary<String,String> headers = bundle.getHeaders();
			String clojure_activator_namespace = headers.get("Clojure-Activator-Namespace");
			if (clojure_activator_namespace != null) {
				IFn func = Clojure.var(clojure_activator_namespace, "start");
				func.invoke(bundle.getBundleContext());
			}
		}
	}
	
	protected void callStop(Bundle bundle) 
	{
		if (isClojureBundle(bundle)) {
			Dictionary<String,String> headers = bundle.getHeaders();
			String clojure_activator_namespace = headers.get("Clojure-Activator-Namespace");
			if (clojure_activator_namespace != null) {
				IFn func = Clojure.var(clojure_activator_namespace, "stop");
				func.invoke(bundle.getBundleContext());
			}
		}
	}
	
	
	@Override
	public void bundleChanged(BundleEvent event) 
	{
		int event_type = event.getType();
		Bundle bundle = event.getBundle();
		logger.fine(String.format("bundleChanged: %s BundleEvent.type is %s",bundle.getSymbolicName(), NamespaceUtil.bundleEventTypeName(event_type)));
		
		switch(event_type) {

			case BundleEvent.RESOLVED:
				init(bundle);
				break;
			case BundleEvent.UNRESOLVED:
				uninit(bundle);
				break;
				
			case BundleEvent.STARTING:
				logger.warning("***** CAUGHT STARTING EVENT: OSGI container may not be compatible with clj-osgi-namepsaces");
				break;
				
			case BundleEvent.STOPPING:
				callStop(bundle);
				break;

			case BundleEvent.STARTED:
				callStart(bundle);
				break;
			case BundleEvent.STOPPED:
				callStop(bundle);
				break;
			case BundleEvent.INSTALLED:
			
			case BundleEvent.UNINSTALLED:
			case BundleEvent.UPDATED:
			default:
				break;
		}
	}
	
	public static boolean isActive() 
	{
		return active;
	}
}


