CREATE TABLE client
(
    id                   BIGINT       NOT NULL,
    version              BIGINT       NULL,
    created_at           datetime     NULL,
    updated_at           datetime     NULL,
    client_name          VARCHAR(255) NULL,
    client_email         VARCHAR(255) NULL,
    contact_person_name  VARCHAR(255) NULL,
    contact_person_email VARCHAR(255) NULL,
    contact_person_phone VARCHAR(255) NULL,
    CONSTRAINT pk_client PRIMARY KEY (id)
);

CREATE TABLE client_staffing_processes
(
    client_id             BIGINT NOT NULL,
    staffing_processes_id BIGINT NOT NULL
);

CREATE TABLE comment
(
    id                  BIGINT       NOT NULL,
    version             BIGINT       NULL,
    created_at          datetime     NULL,
    updated_at          datetime     NULL,
    title               VARCHAR(255) NULL,
    comment             VARCHAR(255) NULL,
    staffing_process_id BIGINT       NULL,
    author_id           BIGINT       NULL,
    comment_parent      BIGINT       NULL,
    CONSTRAINT pk_comment PRIMARY KEY (id)
);

CREATE TABLE employee
(
    id           BIGINT       NOT NULL,
    version      BIGINT       NULL,
    created_at   datetime     NULL,
    updated_at   datetime     NULL,
    name         VARCHAR(255) NULL,
    is_available BIT(1)       NOT NULL,
    user_id      BIGINT       NULL,
    CONSTRAINT pk_employee PRIMARY KEY (id)
);

CREATE TABLE employee_staffing_processes
(
    employee_id           BIGINT NOT NULL,
    staffing_processes_id BIGINT NOT NULL
);

CREATE TABLE staffing_process
(
    id          BIGINT       NOT NULL,
    version     BIGINT       NULL,
    created_at  datetime     NULL,
    updated_at  datetime     NULL,
    title       VARCHAR(255) NULL,
    is_active   BIT(1)       NOT NULL,
    client_id   BIGINT       NULL,
    employee_id BIGINT       NULL,
    CONSTRAINT pk_staffingprocess PRIMARY KEY (id)
);

CREATE TABLE user
(
    id         BIGINT       NOT NULL,
    version    BIGINT       NULL,
    created_at datetime     NULL,
    updated_at datetime     NULL,
    username   VARCHAR(255) NOT NULL,
    first_name VARCHAR(255) NULL,
    last_name  VARCHAR(255) NULL,
    position   VARCHAR(255) NULL,
    email      VARCHAR(255) NULL,
    available  BIT(1)       NOT NULL,
    CONSTRAINT pk_user PRIMARY KEY (id)
);

CREATE TABLE user_roles
(
    user_id BIGINT   NOT NULL,
    roles   SMALLINT NULL
);

CREATE TABLE user_staffing_processes
(
    user_id               BIGINT NOT NULL,
    staffing_processes_id BIGINT NOT NULL
);

ALTER TABLE client_staffing_processes
    ADD CONSTRAINT uc_client_staffing_processes_staffingprocesses UNIQUE (staffing_processes_id);

ALTER TABLE employee_staffing_processes
    ADD CONSTRAINT uc_employee_staffing_processes_staffingprocesses UNIQUE (staffing_processes_id);

ALTER TABLE user_staffing_processes
    ADD CONSTRAINT uc_user_staffing_processes_staffingprocesses UNIQUE (staffing_processes_id);

ALTER TABLE user
    ADD CONSTRAINT uc_user_username UNIQUE (username);

ALTER TABLE comment
    ADD CONSTRAINT FK_COMMENT_ON_AUTHOR FOREIGN KEY (author_id) REFERENCES user (id);

ALTER TABLE comment
    ADD CONSTRAINT FK_COMMENT_ON_STAFFINGPROCESS FOREIGN KEY (staffing_process_id) REFERENCES staffing_process (id);

ALTER TABLE employee
    ADD CONSTRAINT FK_EMPLOYEE_ON_USER FOREIGN KEY (user_id) REFERENCES user (id);

ALTER TABLE staffing_process
    ADD CONSTRAINT FK_STAFFINGPROCESS_ON_CLIENT FOREIGN KEY (client_id) REFERENCES client (id);

ALTER TABLE staffing_process
    ADD CONSTRAINT FK_STAFFINGPROCESS_ON_EMPLOYEE FOREIGN KEY (employee_id) REFERENCES user (id);

ALTER TABLE client_staffing_processes
    ADD CONSTRAINT fk_clistapro_on_client FOREIGN KEY (client_id) REFERENCES client (id);

ALTER TABLE client_staffing_processes
    ADD CONSTRAINT fk_clistapro_on_staffing_process FOREIGN KEY (staffing_processes_id) REFERENCES staffing_process (id);

ALTER TABLE employee_staffing_processes
    ADD CONSTRAINT fk_empstapro_on_employee FOREIGN KEY (employee_id) REFERENCES employee (id);

ALTER TABLE employee_staffing_processes
    ADD CONSTRAINT fk_empstapro_on_staffing_process FOREIGN KEY (staffing_processes_id) REFERENCES staffing_process (id);

ALTER TABLE user_roles
    ADD CONSTRAINT fk_user_roles_on_user FOREIGN KEY (user_id) REFERENCES user (id);

ALTER TABLE user_staffing_processes
    ADD CONSTRAINT fk_usestapro_on_staffing_process FOREIGN KEY (staffing_processes_id) REFERENCES staffing_process (id);

ALTER TABLE user_staffing_processes
    ADD CONSTRAINT fk_usestapro_on_user FOREIGN KEY (user_id) REFERENCES user (id);