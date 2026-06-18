ALTER TABLE "PlayerProfile"
  DROP COLUMN IF EXISTS "licenseNumber";

ALTER TABLE "TournamentRegistration"
  DROP COLUMN IF EXISTS "licenseNumber";
