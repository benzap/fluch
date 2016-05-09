import SimpleHTTPServer
import SocketServer
import os
from os import path

PORT = 8080
ROOT_FOLDER = path.join("..", "..", "resources", "public")

os.chdir(ROOT_FOLDER)
Handler = SimpleHTTPServer.SimpleHTTPRequestHandler
httpd = SocketServer.TCPServer(("", PORT), Handler)
print "Serving Folder {0} at 127.0.0.1:{1}".format(
    ROOT_FOLDER, PORT)

httpd.serve_forever()
