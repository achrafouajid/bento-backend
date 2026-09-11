-- Campaign recipients could only ever carry a phone number, because the only channel that
-- ever enrolled anyone was WhatsApp. Email campaigns now build an audience too, and need
-- somewhere to keep the resolved address (mirroring phone_e164).

ALTER TABLE campaign_recipient ADD COLUMN email VARCHAR(255);
