# HrHTTP
Heart rate monitor HTTP server for WearOS 3.5

Install app, allow sensors read permission and HTTP server should run on your local network (address and port on screen) and localhost.

Heart rate value is accessed from **/hr** endpoint

App should work even if the watch screen goes off (worked for a while, not tested for longer periods, if someone encounters problems I will consider rewriting server and sensor reading as separate service instead of keeping everything in activity)

Tested on **Google Pixel Watch** (build RWDC.230705.001, wear core 1.7.45.534073768)
