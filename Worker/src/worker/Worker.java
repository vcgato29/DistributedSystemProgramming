package worker;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import com.amazonaws.services.sqs.model.Message;

public class Worker
{
	public static void main(String[] args)
	{
		// Initialization:
		  AmazonServices services = new AmazonServices();
		  while(true)
		  {
			  // Receive messages
			  for(Message msg : services.receiveMessages(services.managerWorkerQueueUrl))
			  {
				  // Parse message ( local application id? | url )
				  String[] tokens = services.parseMessage(msg.getBody());
				  String loacalApplicationId = tokens[0];
				  String oldUrl = tokens[1];

				  // remove message
				  System.out.println("deleting message: "+ msg.getBody() + " from queue");
				  services.deleteMessages(services.managerWorkerQueueUrl, msg);
					
				  // Download the image file from url and add text to image
				  String fileNamekey = UUID.randomUUID()+".png";
				  File file = null;
				  
				  try
				  {
					  file = ImageHandler.createStamp(oldUrl, fileNamekey);
				  }
				  catch (IOException e)
				  {
					  e.printStackTrace();
				  }
				  
				  System.out.println("stamp for image: "+ fileNamekey + " was created");
				  
				  // Upload result to S3
				  services.uploadFile(fileNamekey, file);
				  
				  // add message to workerManagerQueue
				  services.sendMessage(services.workerManagerQueueUrl,
						  			   loacalApplicationId + "\t" + oldUrl + "\t" + fileNamekey);
				  
				  System.out.println("image: " + fileNamekey + " was created for: " + loacalApplicationId);
			  }
			  
		  }
	}
	
}