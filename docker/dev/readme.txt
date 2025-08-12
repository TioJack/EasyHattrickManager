https://blog.gcn.sh/howtos/installing-libretranslate-using-docker-and-ubuntu

How I can install it?

first, let's create the directories

mkdir -p /opt/libretranslate/docker
mkdir -p /opt/libretransalte/data/{key,local}


now let's configure the permissions

chown 1032:1032 /opt/libretransalte/data
chown 1032:1032 /opt/libretransalte/data/key
chown 1032:1032 /opt/libretransalte/data/local


then, let's create the docker-compose file

cd /opt/libretranslate/docker
vim docker-compose.yaml


here follows the content, change the parameters for you setup

version: "3"

services:
  libretranslate:
    container_name: libretranslate
    image: libretranslate/libretranslate:v1.3.11
    restart: unless-stopped
    dns:
      - 1.1.1.1
      - 8.8.8.8
    ports:
      - "5000:5000"
    healthcheck:
      test: ['CMD-SHELL', './venv/bin/python scripts/healthcheck.py']
    env_file:
      - libretranslate.env
    volumes:
     - libretranslate_api_keys:/app/db
     - libretranslate_local:/home/libretranslate/.local

volumes:
  libretranslate_api_keys:
    driver_opts:
      type: none
      device: /opt/libretranslate/data/keys
      o: bind
  libretranslate_local:
    driver_opts:
      type: none
      device: /opt/libretranslate/data/local
      o: bind


then, let's create the libetranslate env file

vim libretranslate.env


here follows the content, change the parameters for you setup

LT_DEBUG=true
LT_UPDATE_MODELS=true
LT_SSL=true
LT_SUGGESTIONS=false
LT_METRICS=true

LT_API_KEYS=true

LT_THREADS=12
LT_FRONTEND_TIMEOUT=2000

#LT_REQ_LIMIT=400
#LT_CHAR_LIMIT=1200

LT_API_KEYS_DB_PATH=/app/db/api_keys.db


all right, let's spin up the libretranslate

docker-compose up -d


installing the model files

you should enter the container

docker exec -it libretranslate bash


and run the command to install all languages

for i in `/app/venv/bin/argospm list`;do /app/venv/bin/argospm install $i;done


it will took some time to install, go drink a coffee, then check the directory in your host

$ exit
$ ls -1 /opt/libretranslate/data/local/share/argos-translate/packages/

ar_en
de_en
en_ar
en_de
en_es
en_fi
en_fr
en_ga
en_hi
en_hu
en_id
en_it
en_ja
en_ko
en_nl
en_pl
en_pt
en_sv
en_uk
es_en
fi_en
fr_en
ga_en
hi_en
hu_en
id_en
it_en
ja_en
ko_en
nl_en
pl_en
pt_en
ru_en
sv_en
translate-az_en-1_5
translate-ca_en-1_7
translate-cs_en-1_5
translate-da_en-1_3
translate-el_en-1_5
translate-en_az-1_5
translate-en_ca-1_7
translate-en_cs-1_5
translate-en_da-1_3
translate-en_el-1_5
translate-en_eo-1_5
translate-en_fa-1_5
translate-en_he-1_5
translate-en_ru-1_7
translate-en_sk-1_5
translate-en_th-1_0
translate-en_tr-1_5
translate-en_zh-1_7
translate-eo_en-1_5
translate-fa_en-1_5
translate-he_en-1_5
translate-sk_en-1_5
translate-th_en-1_0
translate-tr_en-1_5
translate-zh_en-1_7
uk_en


Awesome it's all there.

creating the api key

Since we're using “LTAPIKEYS=true” we need to create an API KEY to be able to use libretranslate via API. Let's go to the container again

docker exec -it libretranslate bash


let's create a key with permission to run 120 requests per minute.

/app/venv/bin/ltmanage keys add 120


example of the expected output

libretranslate@ba7f705d97b9:/app$ /app/venv/bin/ltmanage keys
ecae7db0-bolha-us-is-cool-c84c14d2117a: 1200


nice, everything is ready to be used, now let's configure your nginx!

testing the api

curl -XPOST -H "Content-type: application/json" -d '{
"q": "Bolha.io is the most cool project in the fediverso",
"source": "en",
"target": "pt"
}' 'http://localhost:5000/translate'


expected output

{"translatedText":"Bolha.io é o projeto mais legal no fediverso"}