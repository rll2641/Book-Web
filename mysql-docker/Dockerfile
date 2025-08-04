FROM mysql:8.0

ENV MYSQL_ROOT_PASSWORD=1234
ENV MYSQL_DATABASE=Book_Store
ENV MYSQL_USER=jihoon
ENV MYSQL_PASSWORD=1234

EXPOSE 3306

VOLUME ["/var/lib/mysql"]

CMD ["mysqld", "--default-authentication-plugin=mysql_native_password"]
