CREATE TABLE "User" (
    "id" VARCHAR(36) PRIMARY KEY,
    "email" VARCHAR(255) NOT NULL UNIQUE,
    "passwordHash" VARCHAR(255) NOT NULL,
    "name" VARCHAR(120),
    "role" VARCHAR(20) NOT NULL DEFAULT 'PLAYER',
    "createdAt" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "deletedAt" TIMESTAMP
);

CREATE TABLE "Tournament" (
    "id" VARCHAR(36) PRIMARY KEY,
    "slug" VARCHAR(255) NOT NULL UNIQUE,
    "name" VARCHAR(255) NOT NULL,
    "description" TEXT,
    "type" VARCHAR(20) NOT NULL,
    "timeControlType" VARCHAR(20),
    "status" VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    "roundsCount" INTEGER,
    "startDate" TIMESTAMP,
    "endDate" TIMESTAMP,
    "location" VARCHAR(255),
    "createdBy_id" VARCHAR(36) NOT NULL REFERENCES "User"("id"),
    "createdAt" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    "deletedAt" TIMESTAMP
);

CREATE TABLE "Round" (
    "id" VARCHAR(36) PRIMARY KEY,
    "tournament_id" VARCHAR(36) NOT NULL REFERENCES "Tournament"("id"),
    "roundNumber" INTEGER NOT NULL,
    "status" VARCHAR(30) NOT NULL DEFAULT 'NOT_STARTED',
    UNIQUE ("tournament_id", "roundNumber")
);

CREATE TABLE "TournamentRegistration" (
    "id" VARCHAR(36) PRIMARY KEY,
    "tournament_id" VARCHAR(36) NOT NULL REFERENCES "Tournament"("id"),
    "firstName" VARCHAR(80),
    "lastName" VARCHAR(80),
    "email" VARCHAR(255),
    "clubOrCity" VARCHAR(120),
    "federation" VARCHAR(4),
    "rating" INTEGER,
    "chessCategory" VARCHAR(30),
    "phoneNumber" VARCHAR(30),
    "birthYear" INTEGER,
    "notes" TEXT,
    "status" VARCHAR(20) DEFAULT 'REGISTERED',
    "startNumber" INTEGER,
    "createdAt" TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE "Game" (
    "id" VARCHAR(36) PRIMARY KEY,
    "round_id" VARCHAR(36) NOT NULL REFERENCES "Round"("id"),
    "boardNumber" INTEGER NOT NULL,
    "whiteRegistration_id" VARCHAR(36) REFERENCES "TournamentRegistration"("id"),
    "blackRegistration_id" VARCHAR(36) REFERENCES "TournamentRegistration"("id"),
    "result" VARCHAR(20) DEFAULT 'NOT_PLAYED'
);

CREATE TABLE "PlayerProfile" (
    "id" VARCHAR(36) PRIMARY KEY,
    "user_id" VARCHAR(36) NOT NULL UNIQUE REFERENCES "User"("id"),
    "firstName" VARCHAR(80),
    "lastName" VARCHAR(80),
    "email" VARCHAR(255),
    "clubOrCity" VARCHAR(120),
    "federation" VARCHAR(4),
    "classicalRating" INTEGER DEFAULT 0,
    "rapidRating" INTEGER DEFAULT 0,
    "blitzRating" INTEGER DEFAULT 0,
    "chessCategory" VARCHAR(30),
    "phoneNumber" VARCHAR(30),
    "birthYear" INTEGER,
    "licenseNumber" VARCHAR(30)
);

CREATE TABLE "TournamentStanding" (
    "id" VARCHAR(36) PRIMARY KEY,
    "tournament_id" VARCHAR(36) NOT NULL REFERENCES "Tournament"("id"),
    "registration_id" VARCHAR(36) NOT NULL REFERENCES "TournamentRegistration"("id"),
    "points" DOUBLE PRECISION DEFAULT 0,
    "rank" INTEGER,
    UNIQUE ("tournament_id", "registration_id")
);
