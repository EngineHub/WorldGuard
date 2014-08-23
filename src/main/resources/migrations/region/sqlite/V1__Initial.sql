
CREATE TABLE "${tablePrefix}world" (
    id   INTEGER PRIMARY KEY AUTOINCREMENT
                 NOT NULL,
    name TEXT    NOT NULL
                 UNIQUE
);


CREATE TABLE "${tablePrefix}region" (
    id       TEXT    NOT NULL,
    world_id INTEGER NOT NULL
                     REFERENCES "${tablePrefix}world" ( id ) ON DELETE CASCADE
                                                    ON UPDATE CASCADE,
    type     TEXT    NOT NULL,
    priority INTEGER NOT NULL,
    parent   TEXT    DEFAULT ( NULL )
                     --REFERENCES "${tablePrefix}region" ( id ) ON DELETE SET NULL
                     --                                ON UPDATE CASCADE           -- Not supported
                     ,
    PRIMARY KEY ( id, world_id )
);


CREATE TABLE "${tablePrefix}user" (
    id   INTEGER PRIMARY KEY AUTOINCREMENT
                 NOT NULL,
    name TEXT    UNIQUE
                 DEFAULT ( NULL ),
    uuid TEXT    UNIQUE
                 DEFAULT ( NULL )
);


CREATE TABLE "${tablePrefix}group" (
    id   INTEGER PRIMARY KEY AUTOINCREMENT
                 NOT NULL,
    name TEXT    NOT NULL
                 UNIQUE
);


CREATE TABLE "${tablePrefix}region_cuboid" (
    region_id TEXT    NOT NULL,
    world_id  INTEGER NOT NULL,
    min_x     INTEGER NOT NULL,
    min_y     INTEGER NOT NULL,
    min_z     INTEGER NOT NULL,
    max_x     INTEGER NOT NULL,
    max_y     INTEGER NOT NULL,
    max_z     INTEGER NOT NULL,
    PRIMARY KEY ( region_id, world_id ),
    FOREIGN KEY ( region_id, world_id ) REFERENCES "${tablePrefix}region" ( id, world_id ) ON DELETE CASCADE
                                                                                      ON UPDATE CASCADE
);


CREATE TABLE "${tablePrefix}region_poly2d" (
    region_id TEXT    NOT NULL,
    world_id  INTEGER NOT NULL,
    min_y     INTEGER NOT NULL,
    max_y     INTEGER NOT NULL,
    PRIMARY KEY ( region_id, world_id ),
    FOREIGN KEY ( region_id, world_id ) REFERENCES "${tablePrefix}region" ( id, world_id ) ON DELETE CASCADE
                                                                                      ON UPDATE CASCADE
);


CREATE TABLE "${tablePrefix}region_poly2d_point" (
    id        INTEGER PRIMARY KEY AUTOINCREMENT
                      NOT NULL,
    region_id TEXT    NOT NULL,
    world_id  INTEGER NOT NULL,
    x         INTEGER NOT NULL,
    z         INTEGER NOT NULL,
    FOREIGN KEY ( region_id, world_id ) REFERENCES "${tablePrefix}region_poly2d" ( region_id, world_id ) ON DELETE CASCADE
                                                                                                    ON UPDATE CASCADE
);


CREATE TABLE "${tablePrefix}region_groups" (
    region_id TEXT    NOT NULL,
    world_id  INTEGER NOT NULL,
    group_id  INTEGER NOT NULL
                      REFERENCES "${tablePrefix}group" ( id ) ON DELETE CASCADE
                                                     ON UPDATE CASCADE,
    owner     BOOLEAN NOT NULL,
    PRIMARY KEY ( region_id, world_id, group_id ),
    FOREIGN KEY ( region_id, world_id ) REFERENCES "${tablePrefix}region" ( id, world_id ) ON DELETE CASCADE
                                                                                      ON UPDATE CASCADE
);


CREATE TABLE "${tablePrefix}region_flag" (
    id        INTEGER PRIMARY KEY AUTOINCREMENT
                      NOT NULL,
    region_id TEXT    NOT NULL,
    world_id  INTEGER NOT NULL,
    flag      TEXT    NOT NULL,
    value     TEXT    NOT NULL,
    FOREIGN KEY ( region_id, world_id ) REFERENCES "${tablePrefix}region" ( id, world_id ) ON DELETE CASCADE
                                                                                      ON UPDATE CASCADE
);


CREATE TABLE "${tablePrefix}region_players" (
    region_id TEXT    NOT NULL,
    world_id  INTEGER NOT NULL,
    user_id   INTEGER NOT NULL
                      REFERENCES "${tablePrefix}user" ( id ) ON DELETE CASCADE
                                                    ON UPDATE CASCADE,
    owner     BOOLEAN NOT NULL,
    PRIMARY KEY ( region_id, world_id, user_id, owner ),
    FOREIGN KEY ( region_id, world_id ) REFERENCES "${tablePrefix}region" ( id, world_id ) ON DELETE CASCADE
                                                                                      ON UPDATE CASCADE
);


CREATE INDEX "idx_${tablePrefix}region_cuboid_region_id" ON "${tablePrefix}region_cuboid" (
    region_id
);


CREATE INDEX "idx_${tablePrefix}region_world_id" ON "${tablePrefix}region" (
    world_id
);


CREATE INDEX "idx_${tablePrefix}region_parent" ON "${tablePrefix}region" (
    parent
);


CREATE INDEX "idx_${tablePrefix}region_poly2d_region_id" ON "${tablePrefix}region_poly2d" (
    region_id
);


CREATE INDEX "idx_${tablePrefix}region_poly2d_point_region_world_id" ON "${tablePrefix}region_poly2d_point" (
    region_id,
    world_id
);


CREATE INDEX "idx_${tablePrefix}region_groups_region_id" ON "${tablePrefix}region_groups" (
    region_id
);


CREATE INDEX "idx_${tablePrefix}region_groups_group_id" ON "${tablePrefix}region_groups" (
    group_id
);


CREATE INDEX "idx_${tablePrefix}region_flag_region_world_id" ON "${tablePrefix}region_flag" (
    region_id,
    world_id,
    flag
);

