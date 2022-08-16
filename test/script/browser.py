#!/usr/bin/env python3

import os
import os.path
import subprocess
import sys

"""
*********************************
* AMALGAMATED BROWSER.FAILSAFE! *
*********************************

# browser.failsafe.csh Version 5/16/10
# Open a browser window to a specific URL

# For BIRCH 2.7, this script is ONLY called
# from birchconfig (InstallFrame.java) to open
# a web browser when a new install is done. In the
# future, it will be converted to a .sh script
# and will replace browser.csh

# The new thing that this script does is to
# autodetect the platform, whereas the older
# browser.csh depends on BIRCH_PLATFORM being
# set.

# Synopsis: browser.csh  URL

# URL must be fully qualified. For example, for a web site
#     http://home.cc.umanitoba.ca/~frist
#
# For a file, the path does NOT need to begin with file:///
#
"""

def detect_browser():

    if os.environ.has_key("BL_Browser"):
        """
        # BL_Browser is an environment variable telling
        # the command to run your browser.
        # It has been commented out below to allow it to be set
        # by the user's environment
        """
        BL_Browser = os.environ.get("BL_Browser")
    else:
        """
        # if BL_Browser is not set for any reason
        """
        if os.uname()[4] == 'Darwin':
            BL_Browser = 'open'
        else:
            PATH = []
            if os.environ.has_key("PATH"):
                PATH = os.environ.get("PATH").split(os.pathsep)
            for dir in PATH:
                for command in ("firefox", "seamonkey", "mozilla", "opera", "netscape", "iexplore", "explorer"):
                    full_path = os.path.join(dir, command)
                    if os.path.exists(full_path) and os.path.isfile(full_path):
                        BL_Browser = full_path
                        break
                    elif os.path.exists(full_path + ".exe") and os.path.isfile(full_path + ".exe"):
                        BL_Browser = full_path + ".exe"
                        break
    return BL_Browser

def browser(go_url, openurl=False):
    """
    # browser.sh Version 10/ 2/09
    # Open a browser window to a specific URL
    #
    #
    # Synopsis: browser.py  URL
    #
    # URL must be fully qualified. For example, for a web site
    #     http://home.cc.umanitoba.ca/~frist
    #
    # For a file, the path does NOT need to begin with file:///


    @modified: Feb 17 2011
    @author: Graham Alvare
    @contact: alvare@cc.umanitoba.ca
    """

    # constants
    browser_path=detect_browser()

    """
    # Make sure that if URL is a local file, that
    # it is a fully-qualified file path
    """
    if (not go_url.find('://')) or go_url.startswith('file://'):

        if go_url.startswith('file://'):
            go_url = go_url.substring(7)
        """
        # Now, we have to handle cases where the path is specified
        # using an environment variable eg.
        # browser.sh $doc/Phylip/main.html
        """
        go_url = 'file://' + os.path.normpath(os.path.expanduser(os.path.expandvars(go_url)))

    print('Opening document: ' + go_url)

    if browser_path:
        if not openurl:
            """
            # The most recent mozilla family of browsers automatically
            # detects whether there is already a copy of the brower
            # running, which saves a lot of headaches
            """
            subprocess.call([browser_path, go_url])
        else:
            """
            #For older browsers eg. Netscape 4.79 - Netscape 7.1

            # Find out if there is a copy of netscape already running.
            # If there is, call netscape using -remote option, otherwise
            # launch a new netscape job.
            # See http://wp.netscape.com/newsref/std/x-remote.html
            """
            print('abnormal exec')
            if subprocess.call([browser_path, '-remote', 'ping()']):
                subprocess.call([browser_path, go_url])
            else:
                subprocess.call([browser_path, '-remote', "\"openurl(" + go_url + ",new-window)\""])
    else:
        print('failed')

def forkbrowser(go_url, openurl=False):
    pid = os.fork()
    if not pid:
        browser(go_url, openurl)

if __name__ == '__main__':
    """ ensure that there are enough command line arguments to parse """
    if len(sys.argv) < 2:
        print("Missing URL parameter");
        print("");
        print("Usage: browser.py  URL");
        exit();

    """
    # Older versions of netscape/mozilla need to be called using the
    # openURL() argument, if you try to launch a new page while
    # a copy of the browser is already running. The variable BROWSER_OPENURL
    # is set to 0 by default. If you are using an older browser
    # such as Netscape 7.1 and earlier, BROWSER_OPENURL should be set
    # to 1.
    """
    openurl = False;

    if os.environ.has_key("BROWSER_OPENURL") and os.environ.get("BROWSER_OPENURL") >= 1:
        openurl = True

    """ get the URL to open (the first command line argument) """
    browser(sys.argv[1], openurl)
