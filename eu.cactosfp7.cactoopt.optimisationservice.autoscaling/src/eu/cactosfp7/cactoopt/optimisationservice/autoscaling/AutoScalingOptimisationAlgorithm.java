package eu.cactosfp7.cactoopt.optimisationservice.autoscaling;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.util.EcoreUtil;

import eu.cactosfp7.cactoopt.autoscaler.IAutoScaler;
import eu.cactosfp7.cactoopt.optimisationservice.IOptimisationAlgorithm;
import eu.cactosfp7.cactoopt.optimisationservice.registry.OptimisationSettings;
import eu.cactosfp7.cactoopt.util.CDOOptimisationPlanHandler;
import eu.cactosfp7.infrastructuremodels.load.logical.LogicalLoadModel;
import eu.cactosfp7.infrastructuremodels.load.logical.RequestArrivalRateMeasurement;
import eu.cactosfp7.infrastructuremodels.load.logical.ResponseArrivalRateMeasurement;
import eu.cactosfp7.infrastructuremodels.load.physical.PhysicalLoadModel;
import eu.cactosfp7.infrastructuremodels.logicaldc.application.ApplicationInstance;
import eu.cactosfp7.infrastructuremodels.logicaldc.application.ComposedVM;
import eu.cactosfp7.infrastructuremodels.logicaldc.application.ComposedVMImage;
import eu.cactosfp7.infrastructuremodels.logicaldc.application.ScalableVMImageConnector;
import eu.cactosfp7.infrastructuremodels.logicaldc.application.VMImageConnector;
import eu.cactosfp7.infrastructuremodels.logicaldc.application.WhiteBoxApplicationInstance;
import eu.cactosfp7.infrastructuremodels.logicaldc.application.WhiteBoxApplicationTemplate;
import eu.cactosfp7.infrastructuremodels.logicaldc.application.impl.ApplicationPackageImpl;
import eu.cactosfp7.infrastructuremodels.logicaldc.core.LogicalDCModel;
import eu.cactosfp7.infrastructuremodels.physicaldc.core.PhysicalDCModel;
import eu.cactosfp7.optimisationplan.ExecutionStatus;
import eu.cactosfp7.optimisationplan.OptimisationPlan;
import eu.cactosfp7.optimisationplan.OptimisationplanFactory;
import eu.cactosfp7.optimisationplan.ParallelSteps;
import eu.cactosfp7.optimisationplan.ScaleIn;
import eu.cactosfp7.optimisationplan.ScaleOut;
import eu.cactosfp7.optimisationplan.SequentialSteps;

/**
 * The Optimisation Algorithm which encapsulated the auto scaling functionality.
 * The algorithm is periodically called and expected to decide based on the
 * current representation of the data center which scaling actions are supposed
 * to be taken
 * 
 * @author Sebastian Krach, Jakub Krzywda
 *
 */
public class AutoScalingOptimisationAlgorithm implements IOptimisationAlgorithm, IAutoScaler {

	private ApplicationPackageImpl applicationPackage = ApplicationPackageImpl.eINSTANCE;

	/** The logger for this class. */
	private static final Logger log = Logger.getLogger(AutoScalingOptimisationAlgorithm.class.getName());

	public AutoScalingOptimisationAlgorithm() {
	}
	
	private ITimeProvider provider = new ITimeProvider() {
        
        @Override
        public long getCurrentTimeInMs() {
            return System.currentTimeMillis();
        }
    };
    
    public void setTimeProvider(ITimeProvider provider) {
        this.provider = provider;
    }

	@Override
	public OptimisationPlan generateOptimizationPlan(PhysicalDCModel pdcm, LogicalDCModel ldcm, PhysicalLoadModel plm,
			LogicalLoadModel llm) {

		OptimisationPlan plan = null;
		plan = prepareOptimisationPlan(plan);

		List<WhiteBoxApplicationInstance> wbaInstances = getAllWhiteBoxApplicationInstance(ldcm);

		for (WhiteBoxApplicationInstance wbaInstance : wbaInstances) {
			log.info("WhiteBoxApplicationInstance: " + wbaInstance.getId());
			List<ScalableVMImageConnector> scalableConnectors = getAllScalableConnectorOfTemplate(
					(WhiteBoxApplicationTemplate) wbaInstance.getApplicationTemplate());
			for (ScalableVMImageConnector scalableConnector : scalableConnectors) {
				log.info("ScalableVMImageConnector: " + scalableConnector.getId());
				OptimisationPlan planForScalableConnector = optimiseScaling(wbaInstance, scalableConnector, llm);
				if (planForScalableConnector != null) {
					ParallelSteps parallelRootStep = (ParallelSteps) planForScalableConnector.getOptimisationStep();
					if (parallelRootStep.getOptimisationSteps().size() > 0)
						plan = CDOOptimisationPlanHandler.mergeParallelOptimisationPlans(plan,
								planForScalableConnector);
					// plan =
					// CDOOptimisationPlanHandler.mergeOptimisationPlans(plan,
					// planForScalableConnector);

				}
			}
		}

		return plan;
	}

	private void autoscaling(WhiteBoxApplicationInstance wbaInstance, ScalableVMImageConnector scalableConnector,
			LogicalLoadModel llm, OptimisationPlan plan) {
		if (wbaInstance == null) {
			log.severe("White Box Application Instance is missing!");
			return;
		}

		if (scalableConnector == null) {
			log.severe("Scalable VM Image Connector is missing!");
			return;
		}

		if (llm == null) {
			log.severe("Logical Load Model is missing!");
			return;
		}
		
		String autoscalerAlgorithmName = OptimisationSettings.AUTOSCALER_ALGORITHM_NAME;
		// algorithms: Hist, AKTE, Reg, or React
		// int server_speed = 10;
		
		ComposedVMImage scaledNodeImage = (ComposedVMImage) scalableConnector.getServiceProvidedRole().getInterfaceProvidingEntity();
		ComposedVMImage loadBalancerImage = (ComposedVMImage) scalableConnector.getServiceRequiredRole().getInterfaceRequiringEntity();

	    String loadBalancerVMImageName = loadBalancerImage.getName();
	    String scaledVMsImageName = scaledNodeImage.getName();
		
		log.info("Scaled VMs ImageName [" + scaledVMsImageName + "]" + " and Load Balancer VM ImageName ["
				+ loadBalancerVMImageName + "]" + " will be used");

		ComposedVM loadBalancer = getFirstComposedVMByVMImageName(wbaInstance, loadBalancerVMImageName);

		if (loadBalancer == null) {
			log.severe("Load Balancer [" + loadBalancerVMImageName + "] is missing!");
			return;
		}

		EList<ComposedVM> scaledVMs = getAllComposedVMsByVMImageName(wbaInstance, scaledVMsImageName);

		if (scaledVMs.size() == 0) {
			log.severe("There are no VMs [" + scaledVMsImageName + "] in Logical DC Model!");
			return;
		}

		int server_speed = scaledVMs.get(0).getComposedVMImage().getMaximumServableRequests();

		if (server_speed == 0) {
			log.severe("Server speed for [" + scaledVMs.get(0).getComposedVMImage().getName()
					+ "] equals 0! Modify the White Box Application Template model!");
			return;
		}

		Double requestArrivalRate = getRequestArrivalRateMeasurementByName(llm, scalableConnector.getId());
		Double responseArrivalRate = getResponseArrivalRateMeasurementByName(llm, scalableConnector.getId());

		if (requestArrivalRate == null) {
			log.severe("Request arrival rate measurements for  [" + scalableConnector.getId() + "] are missing!");
			return;
		}

		if (responseArrivalRate == null) {
			log.severe("Response arrival rate measurements for  [" + scalableConnector.getId() + "] are missing!");
			return;
		}

		int currentNoVMs = scaledVMs.size();
		int changeNoVMs = callAutoscaler(autoscalerAlgorithmName, server_speed, currentNoVMs,
				responseArrivalRate.intValue(), wbaInstance.getId() + "_" + scalableConnector.getId());

		plan = prepareOptimisationPlan(plan);
		addAutoscalingOptimisationActions(scalableConnector, loadBalancer, scaledVMs, changeNoVMs, plan);
	}

	private WhiteBoxApplicationInstance getWhiteBoxApplicationInstanceByTemplateName(LogicalDCModel ldcm, String name) {
		if (ldcm == null) {
			log.severe("Logical Data Center Model is missing!");
			return null;
		}

		EList<ApplicationInstance> appInstances = ldcm.getApplicationInstances();

		for (ApplicationInstance appInstance : appInstances) {
			WhiteBoxApplicationInstance wbaInstance = this.getModelInstanceById(ldcm, appInstance.getId());
			WhiteBoxApplicationTemplate appTemplate = (WhiteBoxApplicationTemplate) wbaInstance
					.getApplicationTemplate();

			if (appTemplate.getName().startsWith(name)) {
				log.info("White Box Application [" + wbaInstance.getId() + "] is an instance of template ["
						+ appTemplate.getName() + "]");
				return wbaInstance;
			}
		}

		log.warning("There are none White Box Application Instances of a template: " + name
				+ " in the logical data center model!");
		return null;
	}

	private List<WhiteBoxApplicationInstance> getAllWhiteBoxApplicationInstance(LogicalDCModel ldcm) {
		if (ldcm == null) {
			log.severe("Logical Data Center Model is missing!");
			return null;
		}

		EList<ApplicationInstance> appInstances = ldcm.getApplicationInstances();

		List<WhiteBoxApplicationInstance> wbaInstances = new ArrayList<>();

		for (ApplicationInstance appInstance : appInstances) {
			WhiteBoxApplicationInstance wbaInstance = this.getModelInstanceById(ldcm, appInstance.getId());
			
			if (wbaInstance == null) {
				log.info("White Box Application Instance for Application Instance [" + appInstance.getId() + "] is missing!");
				continue;
			}
			
			WhiteBoxApplicationTemplate appTemplate = (WhiteBoxApplicationTemplate) wbaInstance
					.getApplicationTemplate();
			
			if (appTemplate == null) {
				log.info("White Box Application Template for White Box Application Instance [" 
						+ wbaInstance.getId() + "] is missing!");
				continue;
			}
			
			log.info("White Box Application [" + wbaInstance.getId() + "] is an instance of template ["
					+ appTemplate.getName() + "]");
			wbaInstances.add(wbaInstance);
		}

		return wbaInstances;
	}

	/***
	 * Computes the final change of the number of virtual machines taking into
	 * account the minimum and maximum number of instances defined in the
	 * models.
	 * 
	 * @param scalableConnector
	 *            an application layer being scaled
	 * @param loadBalancer
	 *            a load balancer of the application layer being scaled
	 * @param scaledVMs
	 *            a list of current virtual machines instances that are scaled
	 * @param changeNoVMs
	 *            the number of instances to scale-out/in computed by the
	 *            autoscaler algorithm
	 */
	private static void addAutoscalingOptimisationActions(ScalableVMImageConnector scalableConnector,
			ComposedVM loadBalancer, EList<ComposedVM> scaledVMs, int changeNoVMs, OptimisationPlan plan) {

		if (scalableConnector == null) {
			log.severe("Scalable connector is missing!");
			return;
		}

		if (loadBalancer == null) {
			log.severe("Load balancer is missing!");
			return;
		}

		if (scaledVMs == null) {
			log.severe("List of scaled instances is missing!");
			return;
		}

		if (plan == null) {
			log.severe("Optimisation plan is missing!");
			return;
		}

		int currentNoVMs = scaledVMs.size();
		int minNoVMs = scalableConnector.getMinInstances();
		int maxNoVMs = scalableConnector.getMaxInstances();

		if (currentNoVMs == 1)
			log.info("Currently there is 1 VM connected to " + scalableConnector.getId());
//			log.info("Currently there is 1 VM connected to " + loadBalancer.getComposedVMImage().getName());
		else
			log.info("Currently there are " + currentNoVMs + " VMs of " + scalableConnector.getId());
//			log.info("Currently there are " + currentNoVMs + " VMs of " + loadBalancer.getComposedVMImage().getName());

		log.info("Min number of instances for " + scalableConnector.getId() + ": " + minNoVMs);
		log.info("Max number of instances for " + scalableConnector.getId() + ": " + maxNoVMs);
		
		if (changeNoVMs > 0) { // Scale-Out
			int correctedChangeNoVMs = changeNoVMs;

			// never go above the maximum number of instances
			if (currentNoVMs + changeNoVMs > maxNoVMs)
				correctedChangeNoVMs = maxNoVMs - currentNoVMs;

			if (correctedChangeNoVMs > 0)
				log.info("Add " + correctedChangeNoVMs + " instances of " + scaledVMs.get(0).getComposedVMImage().getName()
						+ " to " + loadBalancer.getComposedVMImage().getName());
			else if (correctedChangeNoVMs == 0)
				log.warning("Suggested Scale Out actions will violate the constraints for "
						+ loadBalancer.getComposedVMImage().getName() 
						+ ". No Scale Out actions!");
			else
				log.severe("There are more instances for " 
						+ loadBalancer.getComposedVMImage().getName()
						+ "(" + currentNoVMs + ") than the number specified in the models ("
						+ minNoVMs + "). No Scale Out actions!");
			
			for (int i = 0; i < correctedChangeNoVMs; i++)
				AddScaleOutAction(loadBalancer, scalableConnector, plan);

		} else if (changeNoVMs < 0) { // Scale-In
			int correctedChangeNoVMs = Math.abs(changeNoVMs);

			// never go below the minimum number of instances
			if (currentNoVMs - correctedChangeNoVMs < minNoVMs)
				correctedChangeNoVMs = currentNoVMs - minNoVMs;
				
			if (correctedChangeNoVMs > 0)
				log.info("Remove " + correctedChangeNoVMs + " instances of "
						+ scaledVMs.get(0).getComposedVMImage().getName() + " from "
						+ loadBalancer.getComposedVMImage().getName());
			else if (correctedChangeNoVMs == 0)
				log.warning("Suggested Scale In actions will violate the constraints for "
						+ loadBalancer.getComposedVMImage().getName() 
						+ ". No Scale In actions!");
			else
				log.severe("There are less instances for " 
						+ loadBalancer.getComposedVMImage().getName()
						+ "(" + currentNoVMs + ") than the number specified in the models ("
						+ minNoVMs + "). No Scale In actions!");
			
			for (int i = 0; i < correctedChangeNoVMs; i++) {
				ComposedVM vmToShutDown = scaledVMs.get(i);
				log.info("\tRemove VM: " + vmToShutDown.getId());
				if (vmToShutDown.getVirtualMachine() != null)
					log.info("\t\t[" + vmToShutDown.getVirtualMachine().getName() + "]");
				AddScaleInAction(loadBalancer, vmToShutDown, plan);
			}
		}
	}

	private static Double getRequestArrivalRateMeasurementByName(LogicalLoadModel llm, String vMImageName) {
		EList<RequestArrivalRateMeasurement> requestArrivalRate = llm.getRequestArrivalRateMeasurement();
		if (requestArrivalRate != null) {
			for (RequestArrivalRateMeasurement reqAR : requestArrivalRate) {
				if (reqAR.getObservedVmImageConnector().getId().startsWith(vMImageName)) {
					if (reqAR.getArrivalRate() != null) {
						double reqARValueEst = reqAR.getArrivalRate().getEstimatedValue();
						log.info("Estimated request arrival rate measurements for " + vMImageName + " = "
								+ reqARValueEst);
//						try {
//							long reqARValueExact = reqAR.getArrivalRate().getExactValue();
//							log.info("Exact request arrival rate measurements for " + vMImageName + " = "
//									+ reqARValueExact);
//						} catch (org.jscience.physics.amount.AmountException ae) {
//							log.warning("Exact request arrival rate not available");
//						}
						return new Double(reqARValueEst);
					} else {
						log.warning(
								"Request arrival rate measurement for " + vMImageName + " has value equal to null!");
					}
				}
			}
			log.warning("Request arrival rate measurements for " + vMImageName + " are missing !");
			return null;
		}
		log.warning("There are none request arrival rate measurements in the logical load model!");
		return null;
	}

	private static Double getResponseArrivalRateMeasurementByName(LogicalLoadModel llm, String vMImageName) {
		EList<ResponseArrivalRateMeasurement> responseArrivalRate = llm.getResponseArrivalRateMeasurement();
		if (responseArrivalRate != null) {
			for (ResponseArrivalRateMeasurement respAR : responseArrivalRate) {
				// try {
				// log.info("Response arrival rate for " +
				// respAR.getObservedVmImageConnector().getId() + " equals " +
				// respAR.getArrivalRate().getEstimatedValue());
				// } catch (Error e) {
				// log.info("Response arrival rate for " +
				// respAR.getObservedVmImageConnector().getId() + " not
				// available!");
				// }
				if (respAR.getObservedVmImageConnector().getId().startsWith(vMImageName)) {
					if (respAR.getArrivalRate() != null) {
						double respARValueEst = respAR.getArrivalRate().getEstimatedValue();
						log.info("Estimated response arrival rate measurements for " + vMImageName + " = "
								+ respARValueEst);
//						try {
//							long respARValueExact = respAR.getArrivalRate().getExactValue();
//							log.info("Exact response arrival rate measurements for " + vMImageName + " = "
//									+ respARValueExact);
//						} catch (org.jscience.physics.amount.AmountException ae) {
//							log.warning("Exact response arrival rate not available");
//						}
						return new Double(respARValueEst);
					} else {
						log.warning(
								"Response arrival rate measurement for " + vMImageName + " has value equal to null!");
					}
				}
			}
			log.warning("Response arrival rate measurements for " + vMImageName + " are missing !");
			return null;
		}
		log.warning("There are none response arrival rate measurements in the logical load model!");
		return null;
	}

	private static OptimisationPlan prepareOptimisationPlan(OptimisationPlan plan) {
		// OptimisationPlan modifiedPlan = null;
		SequentialSteps rootStep;
		// ParallelSteps rootStep;
		if (plan == null) {
			log.info("Optimisation plan was null.");
			plan = OptimisationplanFactory.eINSTANCE.createOptimisationPlan();
			log.info("New optimisation plan created.");
		}

		if ((plan != null) && (plan.getOptimisationStep() == null)) {
			log.info("Plan doesn't contain any steps.");
			rootStep = OptimisationplanFactory.eINSTANCE.createSequentialSteps();
			// rootStep =
			// OptimisationplanFactory.eINSTANCE.createParallelSteps();
			plan.setOptimisationStep(rootStep);
			rootStep.setOptimisationPlan(plan);
			rootStep.setExecutionStatus(ExecutionStatus.READY);
			plan.setCreationDate(new Date());
			log.info("Root step added.");
		} else {
			log.info("Reusing existing optimisation plan.");
			// rootStep = (SequentialSteps) plan.getOptimisationStep();
			return plan;
		}

		return plan;
	}

	private static OptimisationPlan prepareParallelOptimisationPlan(OptimisationPlan plan) {
		// OptimisationPlan modifiedPlan = null;
		// SequentialSteps rootStep;
		ParallelSteps rootStep;
		if (plan == null) {
			log.info("Optimisation plan was null.");
			plan = OptimisationplanFactory.eINSTANCE.createOptimisationPlan();
			log.info("New optimisation plan created.");
		}

		if ((plan != null) && (plan.getOptimisationStep() == null)) {
			log.info("Plan doesn't contain any steps.");
			// rootStep =
			// OptimisationplanFactory.eINSTANCE.createSequentialSteps();
			rootStep = OptimisationplanFactory.eINSTANCE.createParallelSteps();
			plan.setOptimisationStep(rootStep);
			rootStep.setOptimisationPlan(plan);
			rootStep.setExecutionStatus(ExecutionStatus.READY);
			plan.setCreationDate(new Date());
			log.info("Root step added.");
		} else {
			log.info("Reusing existing optimisation plan.");
			// rootStep = (SequentialSteps) plan.getOptimisationStep();
			return plan;
		}

		return plan;
	}

	private static EList<ComposedVM> getAllComposedVMsByVMImageName(WhiteBoxApplicationInstance wbaInstance,
			String vMImageName) {
		EList<ComposedVM> vms = new BasicEList<ComposedVM>();

		for (ComposedVM curVM : wbaInstance.getComposedVMs()) {
			if (curVM.getVirtualMachine() == null) {
				log.severe("\t" + "composed_vm[" + curVM.getId() + "] misses a refrerence to a Virtual Machine!");
				continue;
			}
			
			log.info("\t" + "composed_vm_id:" + curVM.getId() + " vm_id: " + curVM.getVirtualMachine().getId()
					+ " vm_name: " + curVM.getVirtualMachine().getName() + " vm_image_name: "
					+ curVM.getComposedVMImage().getName());

			if (curVM.getComposedVMImage().getName().startsWith(vMImageName)) {
				vms.add(curVM);
				log.info("\t\tcomposed_vm_id:" + curVM.getId() + " added");
			}
		}

		if (vms.size() == 0)
			log.warning("No composed VMs with name " + vMImageName + " found!");

		return vms;
	}

	private static ComposedVM getFirstComposedVMByVMImageName(WhiteBoxApplicationInstance wbaInstance,
			String vMImageName) {
		for (ComposedVM curVM : wbaInstance.getComposedVMs()) {
			// log.info("\t"
			// + "composed_vm_id:" + curVM.getId()
			// + " vm_id: " + curVM.getVirtualMachine().getId()
			// + " vm_name: " + curVM.getVirtualMachine().getName()
			// + " vm_image_name: " + curVM.getComposedVMImage().getName()
			// );

			if (curVM.getComposedVMImage().getName().startsWith(vMImageName))
				return curVM;
		}

		log.severe("No composed VM image with name " + vMImageName + " found!");
		return null;
	}

	private static ScalableVMImageConnector getScalableConnectorByName(WhiteBoxApplicationTemplate appTemplate,
			String sVMIname) {

		ScalableVMImageConnector scalableConnector;

		EList<VMImageConnector> connectors = appTemplate.getVmImageConnectors();
		for (VMImageConnector connector : connectors) {
			// log.info("\tconnector_id:" + connector.getId() + "
			// java_object_class:" + connector.getClass().getName());

			if (connector instanceof ScalableVMImageConnector) {
				scalableConnector = (ScalableVMImageConnector) connector;

				if (scalableConnector.getId().startsWith(sVMIname))
					// log.info("\t\tcomposed_vm " + scalableConnector.getId() +
					// " is a ScalableVMImageConnector");
					return scalableConnector;
			}
		}

		log.severe("Scalable Connector [" + sVMIname + "] not found!");
		return null;
	}

	private static List<ScalableVMImageConnector> getAllScalableConnectorOfTemplate(
			WhiteBoxApplicationTemplate appTemplate) {

		List<ScalableVMImageConnector> scalableConnectors = new ArrayList<>();

		EList<VMImageConnector> connectors = appTemplate.getVmImageConnectors();

		for (VMImageConnector connector : connectors) {
			if (connector instanceof ScalableVMImageConnector)
				scalableConnectors.add((ScalableVMImageConnector) connector);
		}

		return scalableConnectors;
	}

	private static void AddScaleOutAction(ComposedVM loadBalancer, ScalableVMImageConnector scalableConnector,
			OptimisationPlan plan) {
		log.info("Creating Scale-Out action [" + loadBalancer.getComposedVMImage().getName() + ", "
				+ scalableConnector.getId() + "]");

		if (loadBalancer == null) {
			log.warning("Creation of Scale-Out action failed. Load balancer not specified.");
			return;
		}

		if (scalableConnector == null) {
			log.warning("Creation of Scale-in action failed. Scalable connector not specified.");
			return;
		}

		if (plan == null) {
			log.warning("Creation of Scale-Out action failed. Optimisation plan not specified.");
			return;
		}

		ScaleOut scaleOutAction = OptimisationplanFactory.eINSTANCE.createScaleOut();
		scaleOutAction.setLoadBalancerInstance(loadBalancer);
		scaleOutAction.setScalingConnector(scalableConnector);
		// scaleOutAction.setSequentialSteps((SequentialSteps)
		// plan.getOptimisationStep());
		scaleOutAction.setParallelSteps((ParallelSteps) plan.getOptimisationStep());
		scaleOutAction.setExecutionStatus(ExecutionStatus.READY);
	}

	private static void AddScaleInAction(ComposedVM loadBalancer, ComposedVM vmToKill, OptimisationPlan plan) {

		log.info("Creating Scale-in action [" + loadBalancer.getComposedVMImage().getName() + "]. Shutting down VM: "
				+ vmToKill.getVirtualMachine().getName() + " [" + vmToKill.getVirtualMachine().getId() + "]");

		if (loadBalancer == null) {
			log.warning("Creation of Scale-in action failed. Load balancer not specified.");
			return;
		}

		if (vmToKill == null) {
			log.warning("Creation of Scale-in action failed. VM to shut down not specified.");
			return;
		}

		if (plan == null) {
			log.warning("Creation of Scale-in action failed. Optimisation plan not specified.");
			return;
		}

		ScaleIn scaleInAction = OptimisationplanFactory.eINSTANCE.createScaleIn();
		scaleInAction.setLoadBalancerInstance(loadBalancer);
		scaleInAction.setScaledVM(vmToKill);
		// scaleInAction.setSequentialSteps((SequentialSteps)
		// plan.getOptimisationStep());
		scaleInAction.setParallelSteps((ParallelSteps) plan.getOptimisationStep());
		scaleInAction.setExecutionStatus(ExecutionStatus.READY);
	}

	private WhiteBoxApplicationInstance getModelInstanceById(LogicalDCModel ldcModel, String applicationInstanceId) {

		Collection<WhiteBoxApplicationInstance> objectsByType = EcoreUtil
				.<WhiteBoxApplicationInstance> getObjectsByType(ldcModel.getApplicationInstances(),
						this.applicationPackage.getWhiteBoxApplicationInstance());
		for (WhiteBoxApplicationInstance curInstance : objectsByType) {
			if (curInstance.getId().equals(applicationInstanceId)) {
				return curInstance;
			}
		}
		return null;
	}

	private int callAutoscaler(String type, int server_speed, int capacity, int load_request, String app_id) {
		// TODO Jakub use this time and pass it.
	    long currentTimeInMs = this.provider.getCurrentTimeInMs();
	    
	    log.info("Calling autoscaler.");
		log.info("Autoscaler input:" + " autoscaler_algorithm=" + type + ", server_speed=" + server_speed
				+ ", capacity=" + capacity + ", load_request=" + load_request + ", app_id=" + app_id
				+ ", current_time=" + currentTimeInMs);

		if (server_speed <= 0) {
			log.warning("Server speed has to be positive. No scaling actions!");
			return 0;
		}

		if (load_request < 0) {
			log.warning("Abnormal measurements of load. No scaling actions!");
			return 0;
		}

		if (capacity <= 0) {
			log.warning("Capacity has to be positive. No scaling actions!");
			return 0;
		}

		try {
			URL url = new URL("http://localhost:5000/" + type + "/v1.0/monitoring/" + app_id);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setDoOutput(true);
			connection.setInstanceFollowRedirects(false);
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "application/json");

			OutputStream os = connection.getOutputStream();
			final PrintStream printStream = new PrintStream(os);
			printStream.print("{\"server_speed\":\"" + server_speed + "\",\"capacity\":\"" + capacity
					+ "\",\"load_requests\":\"" + load_request + "\",\"current_time\":\"" + currentTimeInMs + "\"}");
			printStream.close();
			os.flush();
			os.close();

			InputStream response = connection.getInputStream();
			java.util.Scanner s = new java.util.Scanner(response).useDelimiter("\\A");

			String valueString = s.next();
			// log.info("Autoscaler original output: " + valueString);

			int value = 0;

			try {
				String[] valueStringParts = valueString.split(":|\\.");
//				log.info("Autoscaler output (raw):" + Arrays.toString(valueStringParts));
//				log.info("valueStringParts[1]:" + valueStringParts[1]);
				if (valueStringParts[1].contains("null")) {
					value = 0;
					log.info("Autoscaler returns null (no predictions), converted to " + value);
				} else {
					Double valueDouble = new Double(Double.parseDouble(valueStringParts[1]));
					value = valueDouble.intValue();
					log.info("Autoscaler output (parsed):" + value);
				}
			} catch (Exception e) {
				log.warning("Type conversion error while processing autoscaler output (" + valueString + ")!");
			}

			s.close();
			response.close();
			connection.disconnect();

			return value;
		} catch (ConnectException ce) {
			log.severe("Connection exception. Check if autoscaler has been started.");
			return 0;
		} catch (Exception e) {
			log.severe("Calling autoscaler failed!");
			e.printStackTrace();
			return 0;
		}
	}

	@Override
	public OptimisationPlan optimiseScaling(ApplicationInstance appInstance, ScalableVMImageConnector connector,
			LogicalLoadModel llm) {

		OptimisationPlan plan = null;
		plan = prepareParallelOptimisationPlan(plan);

		WhiteBoxApplicationInstance wbaInstance = null;
		if (appInstance instanceof WhiteBoxApplicationInstance)
			wbaInstance = (WhiteBoxApplicationInstance) appInstance;
		else
			return null;

		autoscaling(wbaInstance, connector, llm, plan);

		return plan;
	}
}
