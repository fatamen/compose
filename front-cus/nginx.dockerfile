FROM nginx:1.27.5
LABEL maintainer="cma@ispan.com.tw"

ENV TZ="Asia/Taipei"
EXPOSE 80/TCP
EXPOSE 6173/TCP
EXPOSE 443/TCP # 確保這裡有 443 端口

COPY ./50x.html /usr/share/nginx/html/50x.html

ARG config_file=nginx.conf
COPY ./${config_file} /etc/nginx/nginx.conf

COPY ./certificate.crt /etc/nginx/certs/certificate.crt
COPY ./private.key /etc/nginx/certs/private.key

ARG publish_dir=dist
COPY ./${publish_dir} /usr/share/nginx/html

CMD ["nginx", "-g", "daemon off;"]