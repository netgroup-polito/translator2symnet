Installation instructions
**Unix** 

Install OpenJDK-8
(If a different version is preferred, the ScalaZ3 jar must be recomplied against this jdk. You're on shaky ground there)
-sudo apt-get update
-sudo apt-get install -y python-software-properties
-sudo add-apt-repository -y ppa:openjdk-r/ppa
-sudo apt-get update
-sudo apt-get install -y openjdk-8-jdk 


(If the link is broken, search sbt on google and grab the latest version from there)
-wget https://dl.bintray.com/sbt/native-packages/sbt/0.13.9/sbt-0.13.9.tgz
-tar xf sbt-0.13.9.tgz
-rm *.tgz

Make sure sbt is in path.
-sudo ln -s ~/sbt/bin/sbt /usr/local/bin/sbt

**Download translator2symnet and Symnet**

-download or clone translator2symnet 
-run generate-jar ant task: it download the Symnet tool in "symnet" folder, modifies the necessary files and creates two .jar in "symnet" folder

The VeriGraph to Symnet converter runs from the translator2symnet/symnet/symnet converter.jar. It generates two folders where it puts the files produced by its execution:
-network: it contains the output files of the VeriGraph input to Symnet input conversion;
-outputs: it contains the output files of the entire execution.

**How to perform converter features**

The VeriGraph to Symnet converter is able to perform several types of executions. The VeriGraph json input file has to be put into the folder translator2symnet/symnet and then, opening the window terminal where the symnet_converter.jar and the policy.jar are, it is possible to perform one of the following :

- java -jar symnet_converter.jar <input_file.json>: it explores the entire network. A packet execution starts from all the nodes capable to generate packets. In the outputs folder, the .output files show the nodes reachable by every other node;

- java -jar symnet_converter.jar <input_file.json> -r <source_node> <destination_node>: it explores the entire network and prints on command line the reachablity (-r) policy result (SAT or UNSAT) between the <source_node> and the  <destination_node>;

- java -jar symnet_converter.jar <input_file.json> -i <source_node> <destination_node> <middleboxe>: it explores the entire network and prints on command line the isolation (-i) policy result; SAT if there is at least one path between the <source_node> and the <destination_node> without traversed the <middleboxe>; UNSAT otherwise;

- java -jar symnet_converter.jar <input_file.json> -t <source_node> <destination_node> <middleboxe>: it explores the entire network and prints on command line the traversal (-t) policy result; SAT if there is at least one path between the <source_node> and the <destination_node> that traverse the <middleboxe>; UNSAT otherwise;

- java -jar symnet_converter.jar <input_file.json> -start <start_node>: it explores the network starting from <start_node> to all the others. In the outputs folder, the <start_node>.output file shows which nodes the <start_node> can reach;

**Check Policies**

Once the execution is performed and the results have been stored in outputs folder, it is possible to check the rechability, isolation and traversal policies.

*Reachability*
java -jar policy.jar -r <source_node> <destination_node>: it prints on command line the reachablity (-r) policy result (SAT or UNSAT) between the <source_node> and the <destination_node>;

*Isolation*
java -jar policy.jar -i <source_node> <destination_node> <middleboxe>: it prints on command line the traversal (-t) policy result; SAT if there is at least one path between the <source_node> and the <destination_node> that traverse the <middleboxe>; UNSAT otherwise;

*Traversal*
java -jar policy.jar -t <source_node> <destination_node> <middleboxe>: it prints on command line the traversal (-t) policy result; SAT if there is at least one path between the <source_node> and the <destination_node> that traverse the <middleboxe>; UNSAT otherwise.

**How to add a new element**
**How to modify packet model**
The packet model can be extended.
In order to add a new layer, perform the following steps:
-edit package.scala file in org.change.v2.util.canonicalnames inserting:
	-the Layer Tag for the desired Layer to add;
	-the new fields (if necessary) with their offset.
-edit the State.scala file in org.change.v2.analysis.memory inserting:
	-the SEFL instructions in order to allocate the new Layer Tag and the new fields.
**Adding new element**
In order to add new features, a scala file containing SEFL instruction must be written. After that, you have to execute the following steps:
	1. Insert the scala file just created into org.change.v2.abstractnet.click.sefl;
	2. Edit BuilderFactory in org.change.v2.abstractnet.click.sefl inserting:

		-case "<element_name>" -> <element_name>.getBuilder(nameValue) in def getBuilder(name Value: String, elementType:String);
		-case "<element_name>" -> <element_name>.getBuilder.

**How to add new VeriGraph elements models**
If the VeriGraph element model that you want to translate into Symnet element model is a new VeriGraph element (see translator2symnet/json folder to know which VeriGraph elements model are already present in translator2symnet), first you have to make this command:
-insert the json schema of the new VeriGraph element into the json folder in translator2symnet.

Then (and also in case of VeriGraph element already present in translator2symnet) you have to make the following:
	1. Create a new <element_type> Java class in it.polito.symnet.model package; so in this class you have to put the combination of Symnet elements that can represent the VeriGraph element you want to model. 
	The element has to contain the following structures:
		```java
		Map<String, String> ports=new HashMap<String, String>();
		Map<String, List<String>> declaration=new HashMap<String, List<String>>();
		Map<String, String> link=new HashMap<String, String>();
		```

	and it has to implement the following methods:
		```java
		public Map<String, String> getPorts(){
			return ports;
		}
		public List<String> generateDeclaration() {}
		public List<String> generateLink(){}
		```

	2. Insert a switch case in getConfiguration() method of Model class in it.polito.symnet.converter package in order to retrieve the configurations of the node (if any);
	3. If the new element does not need an ip address:
		-add the <element_type> in the notAddressing List of the createAddress() method of Model class in it.polito.symnet.converter package.
	4. If the new element contains an IPClassifier() performing the packet forwarding:
		-add the <element_type> in the routing elements List of generateRouting() method of the Model class in it.polito.symnet.converter package;
		-add the <element_type> as a new switch case and add the following code:
			```java
				List<String> n=neighbours.get(name);
				<Element_Type> object name=new <Element_Type>(...);
				Map<String, String> map=new HashMap<String, String>();
				for(String adr : ad){
					if(adr.equals(name))
					continue;
					int found=0;
					if(n.contains(adr)){
						map.put(addresses.get(adr), object_name.getPorts().get(adr));
					}else{
						for(int j=0; j<n.size(); j++){
							String tmp=n.get(j);
							Set<String>visited=new HashSet<String>();
							visited.add(name);
							researchRoute(visited, map, adr , tmp , name , n.get(j), object_name.getPorts(), found, neighbours);
						}
					}
				}
				elements.put(name, map);
				net.setElements(elements);
				net.setObject(name, object_name);
				net.setPorts(object_name.getPorts(), name);
				```
	where <Element_type> is the <element_type> of the new element and the object_name is the name choosen for the <element_type> object.
	5. If the new element does not contain an IPClassifier() performing the packet forwarding:
		-add the creation of the element in an else if statement in createElements() method of Model class in it.polito.symnet.converter package in this way:
			```java
			<Element Type> object_name=new <Element_Type>(...);
			net.setObject(name, object_name);
			net.setPorts(object_name.getPorts(), name);
			```

	6. Rerun generate-jar ant task.


### Tester
In order to start the test you have to put the json graph (VeriGraph input format) of the network that you want to test into the translator2symnet/symnet root folder, and then run one of the following commands:
1. java -jar symnet converter.jar <input_file.json> -test <iteration_number>: it executes hiteration numberi times the exploration of the entire network;
2. java -jar symnet converter.jar <input_file.json> -test -start <start_node> <iteration_number>: it executes <iteration_number> times the exploration of the network starting from <start_node> to all the others.
At the end of the execution, a csv output is generated.


### How to configure the new elements
In order to make improvements, new elements are added to the basic installation. The following elements have been added in org.change.v2.abstractnet.click.sefl

##Generator
The Generator element generates packets by the Fork SEFL instruction and it can also overwrite some packet fields. It can be used with the following patterns as configuration strings:

- web <ip_src> <ip_dest> <body> <application_protocol>: it generates an HTTP packet with the following fields:
	-ip_src: the ip source address of the packet;
	-ip_dst: the ip destination address of the packet;
	-body: an integer that represents the body content of the packet;
	-application_protocol: an integer that represents the application protocol of the packet.

- mail <ip_src> <ip_dest> <email_from> <application_protocol>: it generates a POP3 packet with the following fields:
	-ip:src: the ip source address of the packet;
	-ip_dst: the ip destination address of the packet;
	-email_from: an integer that represents the email_from content of the packet;
	-application_protocol: an integer that represents the application protocol of the packet.

- ip_src <ip_src>: it overwrites the ip source address of the packet;
- ip_dst <ip_dest>: it overwrites the ip destination address of the packet;
- proto <application_protocol>: it overwrites the application protocol field of the packet.

The Generator accepts many configuration strings. An usage example is showed below:

<name> :: Generator(web 192.168.1.1 192.168.1.5 100 1, web 192.168.1.5 192.168.1.10 102 1, web 192.168.1.2 192.168.1.7 101
1): it generates three HTTP packet in the network.


##ApplicationClassifier
The ApplicationClassifier checks the new packet fields added by the improvements. It can be used as follows:

- app proto <application_protocol>: it checks whether the ApplicationProto field of the packet is equal to <application_protocol>;
- body <body>: it checks whether the Body field of the packet is equal to <body>;
- email <email_from>: it checks whether the EmailFrom field of the packet is equal to <email_from>.

The ApplicationClassifier accepts many configuration strings. An usage
example is showed below:
<name> :: ApplicationClassifier(app proto 1, app proto 2, -): it checks if ApplicationProto is equal to 1 or if it is equal to 2, else it performs the action associated with (-).

##Response
<name> :: Response(): it swaps IPsrc=IPdst and TcpSrc=TcpDst fields.







