-- Migration for Module 2 updates

ALTER TABLE installations ADD COLUMN service_id VARCHAR(255);

ALTER TABLE installations 
ADD CONSTRAINT fk_installation_service 
FOREIGN KEY (service_id) REFERENCES services(id);

CREATE TABLE equipements (
    id VARCHAR(255) NOT NULL,
    nom_equipement VARCHAR(255) NOT NULL,
    code_equipement VARCHAR(255) NOT NULL UNIQUE,
    description_equipement VARCHAR(255),
    installation_id VARCHAR(255),
    PRIMARY KEY (id)
);

ALTER TABLE equipements 
ADD CONSTRAINT fk_equipement_installation 
FOREIGN KEY (installation_id) REFERENCES installations(id);
