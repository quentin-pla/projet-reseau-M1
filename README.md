# Projet Réseau - MASTER 1 - 2020
--------------
## Quel est le but ?

C'est un programme réalisé en JAVA permettant de créer un tunnel pour transmettre des paquets d'un réseau IPv6 à travers un réseau IPv4.  
Il ne fonctionne seulement sur des machines virtuelles configurées avec le logiciel Vagrant.

## Configuration

### Logiciels nécessaires sur la machine hôte :
- Vagrant
- Virtualbox
- JAVA

### Package nécessaire sur les machines virtuelles extrémités du tunnel :
- tshark
(nécessaire seulement si l'on souhaite écouter le flux de paquets dans le tunnel)

### Avant l'exécution du programme :
- Les machines virtuelles ont été créées avec Vagrant
- Les machines virtuelles sont en cours d'exécution

## Éxecution

1. Ouvrir un terminal
2. Exécuter le script nommé "tunnel6to4.bash" comprenant plusieurs paramètres :
- Le nom de la machine virtuelle étant la première extrémité du tunnel
- Le nom de la machine virtuelle étant la deuxième extrémité du tunnel
- Le nom de l'interface du tunnel
- L'adresse IP associée à l'interface du tunnel
- (Optionnel) le paramètre "--listen" afin d'écouter le flux de paquets du tunnel
