FROM nginx:1.27.5
LABEL maintainer="cma@ispan.com.tw"

ENV TZ="Asia/Taipei"
EXPOSE 80/TCP
EXPOSE 6173/TCP

COPY ./50x.html /usr/share/nginx/html/50x.html

ARG config_file=nginx.conf
COPY ./${config_file} /etc/nginx/nginx.conf

ARG publish_dir=dist
COPY ./${publish_dir} /usr/share/nginx/html

CMD ["nginx", "-g", "daemon off;"]