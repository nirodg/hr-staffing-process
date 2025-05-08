FROM node:alpine

WORKDIR /usr/src/app

COPY . /usr/src/app

RUN npm install -g @angular/cli

RUN npm install

# Copy entrypoint script
COPY entrypoint.sh /entrypoint.sh
RUN chmod +x entrypoint.sh

RUN cat entrypoint.sh
CMD [ "cat", "entrypoint.sh" ]

ENTRYPOINT ["/entrypoint.sh"]