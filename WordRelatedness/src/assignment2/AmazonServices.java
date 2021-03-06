package assignment2;

import java.io.IOException;
import java.util.ArrayList;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.ec2.model.InstanceType;
import com.amazonaws.services.elasticmapreduce.AmazonElasticMapReduce;
import com.amazonaws.services.elasticmapreduce.AmazonElasticMapReduceClient;
import com.amazonaws.services.elasticmapreduce.model.HadoopJarStepConfig;
import com.amazonaws.services.elasticmapreduce.model.JobFlowInstancesConfig;
import com.amazonaws.services.elasticmapreduce.model.PlacementType;
import com.amazonaws.services.elasticmapreduce.model.RunJobFlowRequest;
import com.amazonaws.services.elasticmapreduce.model.RunJobFlowResult;
import com.amazonaws.services.elasticmapreduce.model.StepConfig;

public class AmazonServices
{
	  public AWSCredentials credentials;
	  AmazonElasticMapReduce mapReduce;
	  JobFlowInstancesConfig instances;
	  ArrayList<StepConfig> steps;
	  HadoopJarStepConfig hadoopJarStep;
	  StepConfig stepConfig;

	  public String bucketName;
	  
	  String jobFlowId;
	  	  
	  public AmazonServices(String k)
	  {
		  this.steps = new ArrayList<StepConfig>();
		  
		  try 
		  {
			  this.credentials = new PropertiesCredentials(AmazonServices.class.getResourceAsStream("../AwsCredentials.properties"));
		  }
		  catch (IOException e) 
		  {
			  e.printStackTrace();
		  } 
		  
		  mapReduce = new AmazonElasticMapReduceClient(credentials);
		   
		  hadoopJarStep = new HadoopJarStepConfig()
		      .withJar("s3n://akiajzfcy5fifmsaagr/step1.jar") // This should be a full map reduce application.
		      .withMainClass("step1.Step1")
		      .withArgs("s3://datasets.elasticmapreduce/ngrams/books/20090715/eng-gb-all/5gram/data", "s3n://akiajzfcy5fifmsaagr/Step1/output");
		  //	s3n://dsp112/eng.corp.10k
		  
		  stepConfig = new StepConfig()
	      .withName("step1")
	      .withHadoopJarStep(hadoopJarStep)
	      .withActionOnFailure("TERMINATE_JOB_FLOW");
		  
		  steps.add(stepConfig);
				  
		  hadoopJarStep =  new HadoopJarStepConfig()
	      .withJar("s3n://akiajzfcy5fifmsaagr/step2.jar") // This should be a full map reduce application.
	      .withMainClass("step2.Step2")
	      .withArgs("s3n://akiajzfcy5fifmsaagr/Step1/output/", "s3n://akiajzfcy5fifmsaagr/Step2/output");
		  
		  stepConfig = new StepConfig()
	      .withName("step2")
	      .withHadoopJarStep(hadoopJarStep)
	      .withActionOnFailure("TERMINATE_JOB_FLOW");
		  
		  steps.add(stepConfig);
		  
		  hadoopJarStep = new HadoopJarStepConfig()
	      .withJar("s3n://akiajzfcy5fifmsaagr/step3.jar") // This should be a full map reduce application.
	      .withMainClass("step3.Step3")
	      .withArgs("s3n://akiajzfcy5fifmsaagr/Step2/output/", "s3n://akiajzfcy5fifmsaagr/Step3/output");
		  
		  stepConfig = new StepConfig()
	      .withName("step3")
	      .withHadoopJarStep(hadoopJarStep)
	      .withActionOnFailure("TERMINATE_JOB_FLOW");
		  
		  steps.add(stepConfig);
		  
		  hadoopJarStep = new HadoopJarStepConfig()
	      .withJar("s3n://akiajzfcy5fifmsaagr/step4.jar") // This should be a full map reduce application.
	      .withMainClass("step4.Step4")
	      .withArgs("s3n://akiajzfcy5fifmsaagr/Step3/output/", "s3n://akiajzfcy5fifmsaagr/Step4/output", k);
		  
		  stepConfig = new StepConfig()
	      .withName("step4")
	      .withHadoopJarStep(hadoopJarStep)
	      .withActionOnFailure("TERMINATE_JOB_FLOW");
		  
		  steps.add(stepConfig);
		  
		  instances = new JobFlowInstancesConfig()
		      .withInstanceCount(19)
		      .withMasterInstanceType(InstanceType.M1Medium.toString())
		      .withSlaveInstanceType(InstanceType.M1Large.toString())
		      .withHadoopVersion("2.2.0")//.withEc2KeyName("manager")
		      .withKeepJobFlowAliveWhenNoSteps(false)
		      .withPlacement(new PlacementType("us-east-1a"));
		   
		  RunJobFlowRequest runFlowRequest = new RunJobFlowRequest()
		      .withName("Assignmet2")
		      .withInstances(instances)
		      .withSteps(steps)
		      .withLogUri("s3n://akiajzfcy5fifmsaagr/logs/");//.withAmiVersion("1.0.0");
		   
		  RunJobFlowResult runJobFlowResult = mapReduce.runJobFlow(runFlowRequest);
		  this.jobFlowId = runJobFlowResult.getJobFlowId();
	  }
		  
	  public static void main(String [] args)
	  {
		  AmazonServices services = new AmazonServices(args[0]);
		  System.out.println("Ran job flow with id: " + services.jobFlowId);
	  }
}

