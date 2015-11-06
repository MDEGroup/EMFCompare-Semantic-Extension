package it.univaq.disim.mdegroup.semantic.emf.compare;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class SemanticEMFCompareRCPPlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "it.univaq.disim.mdegroup.semantic.emf.compare"; //$NON-NLS-1$
	
	// The shared instance
	private static SemanticEMFCompareRCPPlugin plugin;
	

	/**
	 * The constructor
	 */
	public SemanticEMFCompareRCPPlugin() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		SemanticEMFCompareRCPPlugin.plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		SemanticEMFCompareRCPPlugin.plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static SemanticEMFCompareRCPPlugin getDefault() {
		return plugin;
	}
	
}
