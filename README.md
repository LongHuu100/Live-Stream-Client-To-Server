# Live-Stream-Clent-To-Server
App Live stream video từ client (react-native)  to server (NodeJs)

1. RTMP Client publishes data to the RTMP(Media) Server.

2. RTMP Server transcodes the data into either FLV or HLS files(and playlist usually m3u8 and ts) and saves it to storage typically an EFS(Elastic FileSytem) or EBS(Elastic Block Storage).

3. HTTP Server will then serve the transcoded files to the web. It will handle the delivery, rate-limiting, caching, SSL and so on.

4. Video Player will then play the URL served by our HTTP Server. It will handle the buffering and changing the bitrates based on the user's network condition.
