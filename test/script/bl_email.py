#!/usr/bin/env python3

"""
optparse is deprecated in favor of argparse as of Python 2.7. However,
 since 2.7 is not always present on many systems, at this writing,
 it is safer to stick with optparse for now. It should be easy
 to change later, since the syntax is very similar between argparse and optparse.
 from optparse import OptionParser
"""
from optparse import OptionParser

import datetime
import getpass
import os
import re
import stat
import subprocess
import sys
import time

'''
bl_email.py - Send an email message

Synopsis: bl_email.py [--sendeer string] --recipient string --subject string [--message filename]

Message file can contain HTML or text, but will be sent as an HTML message.

@modified: December 5, 2021
@author: Brian Fristensky
@contact: Brian.Fristensky@umanitoba.ca  
'''

#blib = os.environ.get("BIRCHPYLIB")
#sys.path.append(blib)

#from birchlib import Birchmod


PROGRAM = "bl_email.py : "
USAGE = "\n\tUSAGE: bl_email.py [--sender string] --recipient string --subject string [--message filename]"

DEBUG = True
if DEBUG :
    print('bl_email.py: Debugging mode on')

#BM = Birchmod(PROGRAM, USAGE)


# - - - - - - - - - - - - - Utility classes - - - - - - - - - - - - - - - - -

def LocalHostname():
    """
    Return the name of the local machine. Tries a number of methods
    to get a name other than 'localhost' or a null result.
    """
    import socket
    import platform

    def CheckName(name) :
        if name == None or name.startswith("localhost") or name == "" :
            OKAY = False
        else :
            OKAY = True
        return OKAY

    name = os.getenv('HOSTNAME') 

    if not CheckName(name) :
        name = platform.uname()[1]

    if not CheckName(name) :
        if socket.gethostname().find('.')>=0:
            name=socket.gethostname()
        else:
            name=socket.gethostbyaddr(socket.gethostname())[0]

    return name


def SendEmail(From,To,Subject,Text) :
    """
        Very simple email method adapted from:
        http://stackoverflow.com/questions/882712/sending-html-email-using-python
        There are more elaborate examples on this site for sending
        HTML messages and attachments.
    """
    import smtplib
    from email.mime.multipart import MIMEMultipart
    from email.mime.text import MIMEText

    Host = 'localhost'

    msg = MIMEMultipart('alternative')
    msg['Subject'] = Subject
    Html = """\
        <html>
          <head></head>
          <body>
            <p>
            %s
            </p>
          </body>
        </html>
        """ %(Text)
    part1 = MIMEText(Text, 'plain')
    part2 = MIMEText(Html, 'html')
    msg.attach(part1)
    msg.attach(part2)

    try:
       server = smtplib.SMTP(Host)
       server.sendmail(From, To, msg.as_string())
       server.quit()         

       print("Successfully sent email")
    except :
       print("Error: unable to send email")


# - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
class Parameters:
    """
      	Wrapper class for command line parameters
      	"""
    def __init__(self):
        """
     	  Initializes arguments:
                SENDER = ""
                SUBJECT = ""
                MESSAGE = ""
                RECIPIENT = ""


     	  Then calls read_args() to fill in their values from command line
          """
        self.SENDER = ""
        self.SUBJECT = ""
        self.MESSAGE = "" 
        self.RECIPIENT = ""           
        self.read_args()


        if DEBUG :
            print('------------ Parameters from command line ------')
            print('    SENDER: ' + self.SENDER) 
            print('    SUBJECT: ' + self.SUBJECT) 
            print('    MESSAGE: ' + self.MESSAGE)
            print('    RECIPIENT: ' + self.RECIPIENT)
            print()  

    def read_args(self):
        """
        	Read command line arguments into a Parameter object
    	"""
            
        parser = OptionParser()
        parser.add_option("--sender", dest="sender", action="store", default="",
                          help="filename for message body (optional)")
        parser.add_option("--subject", dest="subject", action="store", default="",
                          help="subject for email")
        parser.add_option("--recipient", dest="recipient", action="store", default="",
                          help="address of recipient")
        parser.add_option("--message", dest="message", action="store", default="",
                          help="filename for message body (optional)")

        (options, args) = parser.parse_args() 
        self.SENDER = options.sender
        self.SUBJECT = options.subject
        self.MESSAGE = options.message
        self.RECIPIENT = options.recipient


#======================== MAIN PROCEDURE ==========================
def main():
    """
        Called when not in documentation mode.
        """
	
    # Read parameters from command line
    P = Parameters()

    # Notify user when job is done, if email address was
    # supplied using recipient
    if P.RECIPIENT != "" :
        if P.SENDER == "" :
            Sender = getpass.getuser() + '@' + LocalHostname()
        else :
            Sender = P.SENDER
        Subject = P.SUBJECT
        Message = ""
        if P.MESSAGE != "" :
            if os.path.isfile(P.MESSAGE) :
                MFILE = open(P.MESSAGE,'r')
                for line in MFILE.readlines() :
                    Message = Message + line + '<br>'
                MFILE.close()
        SendEmail(Sender,[P.RECIPIENT],P.SUBJECT,Message)

if __name__ == "__main__":
    main()
#else:
    #used to generate documentation
#    import doctest
#    doctest.testmod()

#if (BM.documentor() or "-test" in sys.argv):
#    pass
#else:
#    main()
