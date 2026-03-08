-- User and Wallet (Tenant Specific)
CREATE TABLE app_user (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    username VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(tenant_id, username)
);

CREATE TABLE wallet (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    user_id UUID NOT NULL REFERENCES app_user(id),
    balance NUMERIC(15, 2) NOT NULL DEFAULT 0.00,
    version BIGINT NOT NULL DEFAULT 0, -- Used for Optimistic Locking
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Matches and Odds (Global across all tenants)
CREATE TABLE match (
    id UUID PRIMARY KEY,
    home_team VARCHAR(255) NOT NULL,
    away_team VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL, -- PRE_MATCH, LIVE, FINISHED
    start_time TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Current Odds for the match (updated frequently by ETL)
CREATE TABLE match_odds (
    id UUID PRIMARY KEY,
    match_id UUID NOT NULL REFERENCES match(id),
    home_win DECIMAL(10, 4) NOT NULL,
    draw DECIMAL(10, 4) NOT NULL,
    away_win DECIMAL(10, 4) NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Bets Placed (Tenant Specific)
CREATE TABLE bet (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    user_id UUID NOT NULL REFERENCES app_user(id),
    match_id UUID NOT NULL REFERENCES match(id),
    prediction VARCHAR(50) NOT NULL, -- HOME_WIN, DRAW, AWAY_WIN
    placed_odds DECIMAL(10, 4) NOT NULL,
    stake NUMERIC(15, 2) NOT NULL,
    status VARCHAR(50) NOT NULL, -- PENDING, WON, LOST, CANCELLED
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Immutable Ledger for all money movements (Tenant Specific)
CREATE TABLE transaction (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    wallet_id UUID NOT NULL REFERENCES wallet(id),
    type VARCHAR(20) NOT NULL, -- DEPOSIT, WITHDRAW, BET_STAKE, BET_WINNING
    amount NUMERIC(15, 2) NOT NULL,
    reference_id UUID, -- Points to a betting ID or external payment ID
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
