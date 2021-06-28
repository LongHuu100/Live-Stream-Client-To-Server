# Live-Stream-Clent-To-Server
App Live stream video từ client (react-native)  to server (NodeJs)

Server using: node-media-server

Client using: react-native-nodemediaclient

<img src="https://media-exp1.licdn.com/dms/image/C4E12AQEcqZcZhKqJ8A/article-inline_image-shrink_1000_1488/0/1582582827896?e=1630540800&v=beta&t=aPRetR9-lGtZyhWtUOHQJ_qEgAppmYajlx6hnJqMIXU" />

1. RTMP Client publishes data to the RTMP(Media) Server.

2. RTMP Server transcodes the data into either FLV or HLS files(and playlist usually m3u8 and ts) and saves it to storage typically an EFS(Elastic FileSytem) or EBS(Elastic Block Storage).

3. HTTP Server will then serve the transcoded files to the web. It will handle the delivery, rate-limiting, caching, SSL and so on.

4. Video Player will then play the URL served by our HTTP Server. It will handle the buffering and changing the bitrates based on the user's network condition.
