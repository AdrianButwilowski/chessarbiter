CREATE TYPE "UserRole" AS ENUM ('ADMIN', 'ARBITER', 'PLAYER');
CREATE TYPE "TournamentType" AS ENUM ('SWISS', 'ROUND_ROBIN');
CREATE TYPE "TimeControlType" AS ENUM ('CLASSICAL', 'RAPID', 'BLITZ');
CREATE TYPE "TournamentStatus" AS ENUM ('DRAFT', 'PUBLISHED', 'REGISTRATION_CLOSED', 'IN_PROGRESS', 'FINISHED', 'CANCELLED');
CREATE TYPE "RegistrationStatus" AS ENUM ('REGISTERED', 'CANCELLED', 'WAITLIST');
CREATE TYPE "RoundStatus" AS ENUM ('NOT_STARTED', 'PAIRINGS_PUBLISHED', 'IN_PROGRESS', 'COMPLETED');
CREATE TYPE "GameResult" AS ENUM ('NOT_PLAYED', 'WHITE_WIN', 'BLACK_WIN', 'DRAW', 'WHITE_FORFEIT', 'BLACK_FORFEIT', 'DOUBLE_FORFEIT', 'BYE');

CREATE TABLE "User" (
  "id" TEXT NOT NULL,
  "email" TEXT NOT NULL,
  "passwordHash" TEXT NOT NULL,
  "name" TEXT,
  "role" "UserRole" NOT NULL DEFAULT 'PLAYER',
  "deletedAt" TIMESTAMP(3),
  "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "updatedAt" TIMESTAMP(3) NOT NULL,
  CONSTRAINT "User_pkey" PRIMARY KEY ("id")
);

CREATE TABLE "PlayerProfile" (
  "id" TEXT NOT NULL,
  "userId" TEXT NOT NULL,
  "firstName" TEXT NOT NULL,
  "lastName" TEXT NOT NULL,
  "email" TEXT NOT NULL,
  "clubOrCity" TEXT NOT NULL,
  "federation" TEXT,
  "licenseNumber" TEXT,
  "classicalRating" INTEGER,
  "rapidRating" INTEGER,
  "blitzRating" INTEGER,
  "chessCategory" TEXT NOT NULL DEFAULT 'NONE',
  "phoneNumber" TEXT,
  "birthYear" INTEGER,
  "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "updatedAt" TIMESTAMP(3) NOT NULL,
  CONSTRAINT "PlayerProfile_pkey" PRIMARY KEY ("id")
);

CREATE TABLE "Tournament" (
  "id" TEXT NOT NULL,
  "title" TEXT NOT NULL,
  "slug" TEXT NOT NULL,
  "description" TEXT NOT NULL,
  "location" TEXT NOT NULL,
  "city" TEXT NOT NULL,
  "startDate" TIMESTAMP(3) NOT NULL,
  "endDate" TIMESTAMP(3),
  "registrationDeadline" TIMESTAMP(3),
  "organizer" TEXT NOT NULL,
  "contactEmail" TEXT NOT NULL,
  "contactPhone" TEXT,
  "tournamentType" "TournamentType" NOT NULL,
  "timeControlType" "TimeControlType" NOT NULL,
  "timeControlDescription" TEXT NOT NULL,
  "rounds" INTEGER NOT NULL,
  "maxPlayers" INTEGER,
  "entryFee" TEXT,
  "regulationsUrl" TEXT,
  "status" "TournamentStatus" NOT NULL DEFAULT 'DRAFT',
  "registrationOpen" BOOLEAN NOT NULL DEFAULT false,
  "allowPlayerCancellation" BOOLEAN NOT NULL DEFAULT true,
  "showRegisteredPlayers" BOOLEAN NOT NULL DEFAULT true,
  "createdById" TEXT NOT NULL,
  "deletedAt" TIMESTAMP(3),
  "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "updatedAt" TIMESTAMP(3) NOT NULL,
  CONSTRAINT "Tournament_pkey" PRIMARY KEY ("id")
);

CREATE TABLE "TournamentRegistration" (
  "id" TEXT NOT NULL,
  "tournamentId" TEXT NOT NULL,
  "userId" TEXT,
  "firstName" TEXT NOT NULL,
  "lastName" TEXT NOT NULL,
  "email" TEXT NOT NULL,
  "clubOrCity" TEXT NOT NULL,
  "federation" TEXT,
  "licenseNumber" TEXT,
  "rating" INTEGER NOT NULL,
  "chessCategory" TEXT NOT NULL DEFAULT 'NONE',
  "phoneNumber" TEXT,
  "birthYear" INTEGER,
  "notes" TEXT,
  "status" "RegistrationStatus" NOT NULL DEFAULT 'REGISTERED',
  "seedNumber" INTEGER,
  "startNumber" INTEGER,
  "finalRank" INTEGER,
  "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "updatedAt" TIMESTAMP(3) NOT NULL,
  CONSTRAINT "TournamentRegistration_pkey" PRIMARY KEY ("id")
);

CREATE TABLE "Round" (
  "id" TEXT NOT NULL,
  "tournamentId" TEXT NOT NULL,
  "roundNumber" INTEGER NOT NULL,
  "status" "RoundStatus" NOT NULL DEFAULT 'NOT_STARTED',
  "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "updatedAt" TIMESTAMP(3) NOT NULL,
  CONSTRAINT "Round_pkey" PRIMARY KEY ("id")
);

CREATE TABLE "Game" (
  "id" TEXT NOT NULL,
  "tournamentId" TEXT NOT NULL,
  "roundId" TEXT NOT NULL,
  "boardNumber" INTEGER NOT NULL,
  "whiteRegistrationId" TEXT,
  "blackRegistrationId" TEXT,
  "result" "GameResult" NOT NULL DEFAULT 'NOT_PLAYED',
  "whitePoints" DOUBLE PRECISION NOT NULL DEFAULT 0,
  "blackPoints" DOUBLE PRECISION NOT NULL DEFAULT 0,
  "resultEnteredById" TEXT,
  "resultEnteredAt" TIMESTAMP(3),
  "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "updatedAt" TIMESTAMP(3) NOT NULL,
  CONSTRAINT "Game_pkey" PRIMARY KEY ("id")
);

CREATE TABLE "TournamentStanding" (
  "id" TEXT NOT NULL,
  "tournamentId" TEXT NOT NULL,
  "registrationId" TEXT NOT NULL,
  "points" DOUBLE PRECISION NOT NULL DEFAULT 0,
  "gamesPlayed" INTEGER NOT NULL DEFAULT 0,
  "wins" INTEGER NOT NULL DEFAULT 0,
  "draws" INTEGER NOT NULL DEFAULT 0,
  "losses" INTEGER NOT NULL DEFAULT 0,
  "forfeits" INTEGER NOT NULL DEFAULT 0,
  "buchholz" DOUBLE PRECISION,
  "medianBuchholz" DOUBLE PRECISION,
  "sonnebornBerger" DOUBLE PRECISION,
  "progressiveScore" DOUBLE PRECISION,
  "directEncounter" DOUBLE PRECISION,
  "rank" INTEGER NOT NULL,
  "updatedAt" TIMESTAMP(3) NOT NULL,
  CONSTRAINT "TournamentStanding_pkey" PRIMARY KEY ("id")
);

CREATE UNIQUE INDEX "User_email_key" ON "User"("email");
CREATE UNIQUE INDEX "User_single_admin_key" ON "User"("role") WHERE "role" = 'ADMIN';
CREATE UNIQUE INDEX "PlayerProfile_userId_key" ON "PlayerProfile"("userId");
CREATE UNIQUE INDEX "Tournament_slug_key" ON "Tournament"("slug");
CREATE UNIQUE INDEX "TournamentRegistration_tournamentId_userId_key" ON "TournamentRegistration"("tournamentId", "userId");
CREATE UNIQUE INDEX "TournamentRegistration_tournamentId_email_key" ON "TournamentRegistration"("tournamentId", "email");
CREATE INDEX "TournamentRegistration_tournamentId_status_idx" ON "TournamentRegistration"("tournamentId", "status");
CREATE UNIQUE INDEX "Round_tournamentId_roundNumber_key" ON "Round"("tournamentId", "roundNumber");
CREATE INDEX "Round_tournamentId_status_idx" ON "Round"("tournamentId", "status");
CREATE UNIQUE INDEX "Game_roundId_boardNumber_key" ON "Game"("roundId", "boardNumber");
CREATE INDEX "Game_tournamentId_roundId_idx" ON "Game"("tournamentId", "roundId");
CREATE UNIQUE INDEX "TournamentStanding_registrationId_key" ON "TournamentStanding"("registrationId");
CREATE UNIQUE INDEX "TournamentStanding_tournamentId_registrationId_key" ON "TournamentStanding"("tournamentId", "registrationId");
CREATE INDEX "TournamentStanding_tournamentId_rank_idx" ON "TournamentStanding"("tournamentId", "rank");

ALTER TABLE "PlayerProfile"
  ADD CONSTRAINT "PlayerProfile_userId_fkey"
  FOREIGN KEY ("userId") REFERENCES "User"("id")
  ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE "Tournament"
  ADD CONSTRAINT "Tournament_createdById_fkey"
  FOREIGN KEY ("createdById") REFERENCES "User"("id")
  ON DELETE RESTRICT ON UPDATE CASCADE;

ALTER TABLE "TournamentRegistration"
  ADD CONSTRAINT "TournamentRegistration_tournamentId_fkey"
  FOREIGN KEY ("tournamentId") REFERENCES "Tournament"("id")
  ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE "TournamentRegistration"
  ADD CONSTRAINT "TournamentRegistration_userId_fkey"
  FOREIGN KEY ("userId") REFERENCES "User"("id")
  ON DELETE SET NULL ON UPDATE CASCADE;

ALTER TABLE "Round"
  ADD CONSTRAINT "Round_tournamentId_fkey"
  FOREIGN KEY ("tournamentId") REFERENCES "Tournament"("id")
  ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE "Game"
  ADD CONSTRAINT "Game_tournamentId_fkey"
  FOREIGN KEY ("tournamentId") REFERENCES "Tournament"("id")
  ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE "Game"
  ADD CONSTRAINT "Game_roundId_fkey"
  FOREIGN KEY ("roundId") REFERENCES "Round"("id")
  ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE "Game"
  ADD CONSTRAINT "Game_whiteRegistrationId_fkey"
  FOREIGN KEY ("whiteRegistrationId") REFERENCES "TournamentRegistration"("id")
  ON DELETE SET NULL ON UPDATE CASCADE;

ALTER TABLE "Game"
  ADD CONSTRAINT "Game_blackRegistrationId_fkey"
  FOREIGN KEY ("blackRegistrationId") REFERENCES "TournamentRegistration"("id")
  ON DELETE SET NULL ON UPDATE CASCADE;

ALTER TABLE "Game"
  ADD CONSTRAINT "Game_resultEnteredById_fkey"
  FOREIGN KEY ("resultEnteredById") REFERENCES "User"("id")
  ON DELETE SET NULL ON UPDATE CASCADE;

ALTER TABLE "TournamentStanding"
  ADD CONSTRAINT "TournamentStanding_tournamentId_fkey"
  FOREIGN KEY ("tournamentId") REFERENCES "Tournament"("id")
  ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE "TournamentStanding"
  ADD CONSTRAINT "TournamentStanding_registrationId_fkey"
  FOREIGN KEY ("registrationId") REFERENCES "TournamentRegistration"("id")
  ON DELETE CASCADE ON UPDATE CASCADE;

CREATE OR REPLACE FUNCTION "protect_single_admin"()
RETURNS TRIGGER AS $$
BEGIN
  IF TG_OP = 'DELETE' AND OLD."role" = 'ADMIN' THEN
    RAISE EXCEPTION 'The administrator account cannot be deleted.';
  END IF;

  IF TG_OP = 'DELETE' THEN
    RETURN OLD;
  END IF;

  IF TG_OP = 'UPDATE' AND OLD."role" = 'ADMIN' AND NEW."role" <> 'ADMIN' THEN
    RAISE EXCEPTION 'The administrator role cannot be removed.';
  END IF;

  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER "User_protect_single_admin"
BEFORE UPDATE OR DELETE ON "User"
FOR EACH ROW
EXECUTE FUNCTION "protect_single_admin"();
