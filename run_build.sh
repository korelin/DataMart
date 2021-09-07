echo "Please enter path to recources directory (for example /home/ivan/repo/data/):"
read DIR
docker build -t korelin.solution .
docker run --volume $DIR:/home/ubuntu/data/ korelin.solution