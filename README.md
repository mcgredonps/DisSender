# DisSender
A Java Netbeans project to send Distributed Interactive Simulation (DIS) Entity State Protocol Data Units (PDUs) to a broadcast address destination. With relative ease it may also send other PDU types. 

The program was developed in Netbeats with ant. You should run it in conjuction with DisReceiver, another git repository, which reads broadcast UDP traffic. See https://github.com/mcgredonps/DisReceiver

Run both at the same time and see the messages exchanged between the DisSender and DisReceiver applications. You will probably have to change the TCP/IP addresses used to match whatever has been assigned in your network. 
