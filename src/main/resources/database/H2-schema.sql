DROP TABLE IF EXISTS task;
DROP TABLE IF EXISTS member;

CREATE TABLE task (
  id CHAR(36),
  title TEXT NOT NULL,
  description TEXT,
  deadline_date DATE,
  finished BOOLEAN NOT NULL,
  finished_at TIMESTAMP,
  created_at TIMESTAMP NOT NULL,
  version BIGINT NOT NULL,
  CONSTRAINT pk_task PRIMARY KEY (id)
);

CREATE INDEX ix_task_create_at ON task(created_at);
CREATE INDEX ix_task_deadline_date ON task(deadline_date);
CREATE INDEX ix_task_finished ON task(finished);

CREATE TABLE member (
  id CHAR(36),
  login_id VARCHAR(256) NOT NULL,
  name TEXT,
  version BIGINT NOT NULL,
  CONSTRAINT pk_member PRIMARY KEY (id)
);

CREATE UNIQUE INDEX uk_member_login_id ON member(login_id);

COMMIT;