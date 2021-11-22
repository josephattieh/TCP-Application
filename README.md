# TCP-Chat-Application

_Submitted on April 2018 for the course COE 431 - Computer Networks_

# Requirements

## General Overview

For this project, we are required to develop the server and client sides of a secure
messaging application.  The chatting application that we are tasked
with developing consists of the following essential building blocks, namely:  a) a _server program_ that is mainly responsible for authenticating chat clients and for relaying chat message over TCP from one chat client to another; and b) a _client program_ that should be able, among others, to log himself in through valid username
and password and to interact with the other chat clients through messages
exchanged via the server. More detailed technical specification of the functionalities
to be supported by both the server and client programs are supplied in the
subsequent sections.

## Client and Server Programs

The server program is required to maintain a special text file “user_pass.txt” storing
the following collection of username and password pairs:

_Beirut 117subway_

_COE312 LotsOfExams_

_Facebook WastingTime_

_Whatsapp EveryDay_

_Wikepedia UnreliableInfo_



When we start the server, it should read-in the list of username and password pairs,
listen to a specific port, and start waiting for clients contact. When interacting with
the server, the client should proceed through the following phases:

1. An authentication phase, during which the server prompts the user to provide
    their username and password. If the password supplied by the user is invalid,
    the server should ask the user to attempt again, but the user is not entitled to
    more than 3 consecutive failed attempts. After the third failed attempt, the
    server should block that user’s access based on his IP address for 2 minutes.

2. Once the user has been authenticated, it should be allowed to perform the
    following tasks through the help of the server:
       a. A user should be able to view a list of all the users that are currently
          online
       b. A user should be able to list all of the users who were active in the
          past 60 minutes.
       c. A user should be able to engage in a private conversation with other
          users via the server. If the other user that a user wishes to send
          private messages to is not online, the server should store the received
          messages and deliver them to recipient user when the latter becomes
          online.
       d. A user should be able to send a broadcast message to all of the users
          that are available online.
       e. A user should be able to block another user from sending him/her any
          further messages.
       f. A user should be able to unblock a previously blocked one.
       g. When a user is done, he/she should be able to logout from the server.
       h. If the client is inactive for 15 minutes, the server program should
          automatically log that user out.


Finally, the server program should provide
support for the list of routines given in the below-enclosed table.

|Routine             | Functionality                                                         |
| --- | --- |
|WhoseOnline         | Lists names of all connected users                                    |
|WhoLastHr           |Lists names of users that connected to server in the last hour.        |
|Broadcast <msg>     |Broadcasts the specified message to all online users.                  |
|Message <user> <msg>| Send the specified message privately to the specified user            |
|Block <user>        | Blocks the specified user by preventing him from sending any messages |
|Ublock <user>       | Unblocks the already blocked specified user                           |
|Logout              | Log out the specified user                                            |




## License
No license.
