
apt -y install python3-pip
apt -y install openjdk-11-jdk
apt -y install docker
service docker start

pip3 install --user ansible==2.9.10
pip3 install --user pymongo

ansible-galaxy collection install community.general:==1.3.2
ansible-galaxy collection install community.docker:==1.2.1
ansible-galaxy collection install community.mongodb
ansible-galaxy install kami911.java_open_jdk11

(cd spring-boot-kafka-event-generator && sh gradlew dockerBuildImage)
(cd skark-streams-writer && sh gradlew jar)

chmod o-w ansible
(cd ansible && ansible-playbook playbook.yml)




