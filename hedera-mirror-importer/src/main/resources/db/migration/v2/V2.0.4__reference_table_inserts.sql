-------------------
-- Insert values into reference tables
-------------------

-- t_transaction_results
insert into t_transaction_results (result, proto_id)
values ('OK', 0),
       ('INVALID_TRANSACTION', 1),
       ('PAYER_ACCOUNT_NOT_FOUND', 2),
       ('INVALID_NODE_ACCOUNT', 3),
       ('TRANSACTION_EXPIRED', 4),
       ('INVALID_TRANSACTION_START', 5),
       ('INVALID_TRANSACTION_DURATION', 6),
       ('INVALID_SIGNATURE', 7),
       ('MEMO_TOO_LONG', 8),
       ('INSUFFICIENT_TX_FEE', 9),
       ('INSUFFICIENT_PAYER_BALANCE', 10),
       ('DUPLICATE_TRANSACTION', 11),
       ('BUSY', 12),
       ('NOT_SUPPORTED', 13),
       ('INVALID_FILE_ID', 14),
       ('INVALID_ACCOUNT_ID', 15),
       ('INVALID_CONTRACT_ID', 16),
       ('INVALID_TRANSACTION_ID', 17),
       ('RECEIPT_NOT_FOUND', 18),
       ('RECORD_NOT_FOUND', 19),
       ('INVALID_SOLIDITY_ID', 20),
       ('UNKNOWN', 21),
       ('SUCCESS', 22),
       ('FAIL_INVALID', 23),
       ('FAIL_FEE', 24),
       ('FAIL_BALANCE', 25),
       ('KEY_REQUIRED', 26),
       ('BAD_ENCODING', 27),
       ('INSUFFICIENT_ACCOUNT_BALANCE', 28),
       ('INVALID_SOLIDITY_ADDRESS', 29),
       ('INSUFFICIENT_GAS', 30),
       ('CONTRACT_SIZE_LIMIT_EXCEEDED', 31),
       ('LOCAL_CALL_MODIFICATION_EXCEPTION', 32),
       ('CONTRACT_REVERT_EXECUTED', 33),
       ('CONTRACT_EXECUTION_EXCEPTION', 34),
       ('INVALID_RECEIVING_NODE_ACCOUNT', 35),
       ('MISSING_QUERY_HEADER', 36),
       ('ACCOUNT_UPDATE_FAILED', 37),
       ('INVALID_KEY_ENCODING', 38),
       ('NULL_SOLIDITY_ADDRESS', 39),
       ('CONTRACT_UPDATE_FAILED', 40),
       ('INVALID_QUERY_HEADER', 41),
       ('INVALID_FEE_SUBMITTED', 42),
       ('INVALID_PAYER_SIGNATURE', 43),
       ('KEY_NOT_PROVIDED', 44),
       ('INVALID_EXPIRATION_TIME', 45),
       ('NO_WACL_KEY', 46),
       ('FILE_CONTENT_EMPTY', 47),
       ('INVALID_ACCOUNT_AMOUNTS', 48),
       ('EMPTY_TRANSACTION_BODY', 49),
       ('INVALID_TRANSACTION_BODY', 50),
       ('INVALID_SIGNATURE_TYPE_MISMATCHING_KEY', 51),
       ('INVALID_SIGNATURE_COUNT_MISMATCHING_KEY', 52),
       ('EMPTY_CLAIM_BODY', 53),
       ('EMPTY_CLAIM_HASH', 54),
       ('EMPTY_CLAIM_KEYS', 55),
       ('INVALID_CLAIM_HASH_SIZE', 56),
       ('EMPTY_QUERY_BODY', 57),
       ('EMPTY_CLAIM_QUERY', 58),
       ('CLAIM_NOT_FOUND', 59),
       ('ACCOUNT_ID_DOES_NOT_EXIST', 60),
       ('CLAIM_ALREADY_EXISTS', 61),
       ('INVALID_FILE_WACL', 62),
       ('SERIALIZATION_FAILED', 63),
       ('TRANSACTION_OVERSIZE', 64),
       ('TRANSACTION_TOO_MANY_LAYERS', 65),
       ('CONTRACT_DELETED', 66),
       ('PLATFORM_NOT_ACTIVE', 67),
       ('KEY_PREFIX_MISMATCH', 68),
       ('PLATFORM_TRANSACTION_NOT_CREATED', 69),
       ('INVALID_RENEWAL_PERIOD', 70),
       ('INVALID_PAYER_ACCOUNT_ID', 71),
       ('ACCOUNT_DELETED', 72),
       ('FILE_DELETED', 73),
       ('ACCOUNT_REPEATED_IN_ACCOUNT_AMOUNTS', 74),
       ('SETTING_NEGATIVE_ACCOUNT_BALANCE', 75),
       ('OBTAINER_REQUIRED', 76),
       ('OBTAINER_SAME_CONTRACT_ID', 77),
       ('OBTAINER_DOES_NOT_EXIST', 78),
       ('MODIFYING_IMMUTABLE_CONTRACT', 79),
       ('FILE_SYSTEM_EXCEPTION', 80),
       ('AUTORENEW_DURATION_NOT_IN_RANGE', 81),
       ('ERROR_DECODING_BYTESTRING', 82),
       ('CONTRACT_FILE_EMPTY', 83),
       ('CONTRACT_BYTECODE_EMPTY', 84),
       ('INVALID_INITIAL_BALANCE', 85),
       ('INVALID_RECEIVE_RECORD_THRESHOLD', 86),
       ('INVALID_SEND_RECORD_THRESHOLD', 87),
       ('ACCOUNT_IS_NOT_GENESIS_ACCOUNT', 88),
       ('PAYER_ACCOUNT_UNAUTHORIZED', 89),
       ('INVALID_FREEZE_TRANSACTION_BODY', 90),
       ('FREEZE_TRANSACTION_BODY_NOT_FOUND', 91),
       ('TRANSFER_LIST_SIZE_LIMIT_EXCEEDED', 92),
       ('RESULT_SIZE_LIMIT_EXCEEDED', 93),
       ('NOT_SPECIAL_ACCOUNT', 94),
       ('CONTRACT_NEGATIVE_GAS', 95),
       ('CONTRACT_NEGATIVE_VALUE', 96),
       ('INVALID_FEE_FILE', 97),
       ('INVALID_EXCHANGE_RATE_FILE', 98),
       ('INSUFFICIENT_LOCAL_CALL_GAS', 99),
       ('ENTITY_NOT_ALLOWED_TO_DELETE', 100),
       ('AUTHORIZATION_FAILED', 101),
       ('FILE_UPLOADED_PROTO_INVALID', 102),
       ('FILE_UPLOADED_PROTO_NOT_SAVED_TO_DISK', 103),
       ('FEE_SCHEDULE_FILE_PART_UPLOADED', 104),
       ('EXCHANGE_RATE_CHANGE_LIMIT_EXCEEDED', 105),
       ('MAX_CONTRACT_STORAGE_EXCEEDED', 106),
       ('MAX_GAS_LIMIT_EXCEEDED', 111),
       ('MAX_FILE_SIZE_EXCEEDED', 112),
       ('INVALID_TOPIC_ID', 150),
       ('INVALID_ADMIN_KEY', 155),
       ('INVALID_SUBMIT_KEY', 156),
       ('UNAUTHORIZED', 157),
       ('INVALID_TOPIC_MESSAGE', 158),
       ('INVALID_AUTORENEW_ACCOUNT', 159),
       ('AUTORENEW_ACCOUNT_NOT_ALLOWED', 160),
       ('TOPIC_EXPIRED', 162),
       ('INVALID_CHUNK_NUMBER', 163),
       ('INVALID_CHUNK_TRANSACTION_ID', 164),
       ('ACCOUNT_FROZEN_FOR_TOKEN', 165),
       ('TOKENS_PER_ACCOUNT_LIMIT_EXCEEDED', 166),
       ('INVALID_TOKEN_ID', 167),
       ('INVALID_TOKEN_DECIMALS', 168),
       ('INVALID_TOKEN_INITIAL_SUPPLY', 169),
       ('INVALID_TREASURY_ACCOUNT_FOR_TOKEN', 170),
       ('INVALID_TOKEN_SYMBOL', 171),
       ('TOKEN_HAS_NO_FREEZE_KEY', 172),
       ('TRANSFERS_NOT_ZERO_SUM_FOR_TOKEN', 173),
       ('MISSING_TOKEN_SYMBOL', 174),
       ('TOKEN_SYMBOL_TOO_LONG', 175),
       ('ACCOUNT_KYC_NOT_GRANTED_FOR_TOKEN', 176),
       ('TOKEN_HAS_NO_KYC_KEY', 177),
       ('INSUFFICIENT_TOKEN_BALANCE', 178),
       ('TOKEN_WAS_DELETED', 179),
       ('TOKEN_HAS_NO_SUPPLY_KEY', 180),
       ('TOKEN_HAS_NO_WIPE_KEY', 181),
       ('INVALID_TOKEN_MINT_AMOUNT', 182),
       ('INVALID_TOKEN_BURN_AMOUNT', 183),
       ('TOKEN_NOT_ASSOCIATED_TO_ACCOUNT', 184),
       ('CANNOT_WIPE_TOKEN_TREASURY_ACCOUNT', 185),
       ('INVALID_KYC_KEY', 186),
       ('INVALID_WIPE_KEY', 187),
       ('INVALID_FREEZE_KEY', 188),
       ('INVALID_SUPPLY_KEY', 189),
       ('MISSING_TOKEN_NAME', 190),
       ('TOKEN_NAME_TOO_LONG', 191),
       ('INVALID_WIPING_AMOUNT', 192),
       ('TOKEN_IS_IMMUTABLE', 193),
       ('TOKEN_ALREADY_ASSOCIATED_TO_ACCOUNT', 194),
       ('TRANSACTION_REQUIRES_ZERO_TOKEN_BALANCES', 195),
       ('ACCOUNT_IS_TREASURY', 196),
       ('TOKEN_ID_REPEATED_IN_TOKEN_LIST', 197),
       ('TOKEN_TRANSFER_LIST_SIZE_LIMIT_EXCEEDED', 198),
       ('EMPTY_TOKEN_TRANSFER_BODY', 199),
       ('EMPTY_TOKEN_TRANSFER_ACCOUNT_AMOUNTS', 200),
       ('INVALID_SCHEDULE_ID', 201),
       ('SCHEDULE_IS_IMMUTABLE', 202),
       ('INVALID_SCHEDULE_PAYER_ID', 203),
       ('INVALID_SCHEDULE_ACCOUNT_ID', 204),
       ('NO_NEW_VALID_SIGNATURES', 205),
       ('UNRESOLVABLE_REQUIRED_SIGNERS', 206),
       ('SCHEDULED_TRANSACTION_NOT_IN_WHITELIST', 207),
       ('SOME_SIGNATURES_WERE_INVALID', 208),
       ('TRANSACTION_ID_FIELD_NOT_ALLOWED', 209),
       ('IDENTICAL_SCHEDULE_ALREADY_CREATED', 210),
       ('INVALID_ZERO_BYTE_IN_STRING', 211),
       ('SCHEDULE_ALREADY_DELETED', 212),
       ('SCHEDULE_ALREADY_EXECUTED', 213),
       ('MESSAGE_SIZE_TOO_LARGE', 214),
       ('OPERATION_REPEATED_IN_BUCKET_GROUPS', 215),
       ('BUCKET_CAPACITY_OVERFLOW', 216),
       ('NODE_CAPACITY_NOT_SUFFICIENT_FOR_OPERATION', 217),
       ('BUCKET_HAS_NO_THROTTLE_GROUPS', 218),
       ('THROTTLE_GROUP_HAS_ZERO_OPS_PER_SEC', 219),
       ('SUCCESS_BUT_MISSING_EXPECTED_OPERATION', 220),
       ('UNPARSEABLE_THROTTLE_DEFINITIONS', 221),
       ('INVALID_THROTTLE_DEFINITIONS', 222),
       ('ACCOUNT_EXPIRED_AND_PENDING_REMOVAL', 223),
       ('INVALID_TOKEN_MAX_SUPPLY', 224),
       ('INVALID_TOKEN_NFT_SERIAL_NUMBER', 225),
       ('INVALID_NFT_ID', 226),
       ('METADATA_TOO_LONG', 227),
       ('BATCH_SIZE_LIMIT_EXCEEDED', 228),
       ('QUERY_RANGE_LIMIT_EXCEEDED', 229),
       ('FRACTION_DIVIDES_BY_ZERO', 230),
       ('INSUFFICIENT_PAYER_BALANCE_FOR_CUSTOM_FEE', 231),
       ('CUSTOM_FEES_LIST_TOO_LONG', 232),
       ('INVALID_CUSTOM_FEE_COLLECTOR', 233),
       ('INVALID_TOKEN_ID_IN_CUSTOM_FEES', 234),
       ('TOKEN_NOT_ASSOCIATED_TO_FEE_COLLECTOR', 235),
       ('TOKEN_MAX_SUPPLY_REACHED', 236),
       ('SENDER_DOES_NOT_OWN_NFT_SERIAL_NO', 237),
       ('CUSTOM_FEE_NOT_FULLY_SPECIFIED', 238),
       ('CUSTOM_FEE_MUST_BE_POSITIVE', 239),
       ('TOKEN_HAS_NO_FEE_SCHEDULE_KEY', 240),
       ('CUSTOM_FEE_OUTSIDE_NUMERIC_RANGE', 241),
       ('ROYALTY_FRACTION_CANNOT_EXCEED_ONE', 242),
       ('FRACTIONAL_FEE_MAX_AMOUNT_LESS_THAN_MIN_AMOUNT', 243),
       ('CUSTOM_SCHEDULE_ALREADY_HAS_NO_FEES', 244),
       ('CUSTOM_FEE_DENOMINATION_MUST_BE_FUNGIBLE_COMMON', 245),
       ('CUSTOM_FRACTIONAL_FEE_ONLY_ALLOWED_FOR_FUNGIBLE_COMMON', 246),
       ('INVALID_CUSTOM_FEE_SCHEDULE_KEY', 247),
       ('INVALID_TOKEN_MINT_METADATA', 248),
       ('INVALID_TOKEN_BURN_METADATA', 249),
       ('CURRENT_TREASURY_STILL_OWNS_NFTS', 250),
       ('ACCOUNT_STILL_OWNS_NFTS', 251),
       ('TREASURY_MUST_OWN_BURNED_NFT', 252),
       ('ACCOUNT_DOES_NOT_OWN_WIPED_NFT', 253),
       ('ACCOUNT_AMOUNT_TRANSFERS_ONLY_ALLOWED_FOR_FUNGIBLE_COMMON', 254),
       ('MAX_NFTS_IN_PRICE_REGIME_HAVE_BEEN_MINTED', 255),
       ('PAYER_ACCOUNT_DELETED', 256),
       ('CUSTOM_FEE_CHARGING_EXCEEDED_MAX_RECURSION_DEPTH', 257),
       ('CUSTOM_FEE_CHARGING_EXCEEDED_MAX_ACCOUNT_AMOUNTS', 258),
       ('INSUFFICIENT_SENDER_ACCOUNT_BALANCE_FOR_CUSTOM_FEE', 259),
       ('SERIAL_NUMBER_LIMIT_REACHED', 260),
       ('CUSTOM_ROYALTY_FEE_ONLY_ALLOWED_FOR_NON_FUNGIBLE_UNIQUE', 261),
       ('NO_REMAINING_AUTOMATIC_ASSOCIATIONS', 262),
       ('EXISTING_AUTOMATIC_ASSOCIATIONS_EXCEED_GIVEN_LIMIT', 263),
       ('REQUESTED_NUM_AUTOMATIC_ASSOCIATIONS_EXCEEDS_ASSOCIATION_LIMIT', 264),
       ('TOKEN_IS_PAUSED', 265),
       ('TOKEN_HAS_NO_PAUSE_KEY', 266),
       ('INVALID_PAUSE_KEY', 267),
       ('FREEZE_UPDATE_FILE_DOES_NOT_EXIST', 268),
       ('FREEZE_UPDATE_FILE_HASH_DOES_NOT_MATCH', 269),
       ('NO_UPGRADE_HAS_BEEN_PREPARED', 270),
       ('NO_FREEZE_IS_SCHEDULED', 271),
       ('UPDATE_FILE_HASH_CHANGED_SINCE_PREPARE_UPGRADE', 272),
       ('FREEZE_START_TIME_MUST_BE_FUTURE', 273),
       ('PREPARED_UPDATE_FILE_IS_IMMUTABLE', 274),
       ('FREEZE_ALREADY_SCHEDULED', 275),
       ('FREEZE_UPGRADE_IN_PROGRESS', 276),
       ('UPDATE_FILE_ID_DOES_NOT_MATCH_PREPARED', 277),
       ('UPDATE_FILE_HASH_DOES_NOT_MATCH_PREPARED', 278);

-- t_transaction_types
insert into t_transaction_types (proto_id, name, entity_type)
values (7, 'CONTRACTCALL', 'CONTRACT'),
       (8, 'CONTRACTCREATEINSTANCE', 'CONTRACT'),
       (9, 'CONTRACTUPDATEINSTANCE', 'CONTRACT'),
       (10, 'CRYPTOADDLIVEHASH', 'ACCOUNT'),
       (11, 'CRYPTOCREATEACCOUNT', 'ACCOUNT'),
       (12, 'CRYPTODELETE', 'ACCOUNT'),
       (13, 'CRYPTODELETELIVEHASH', 'ACCOUNT'),
       (14, 'CRYPTOTRANSFER', 'ACCOUNT'),
       (15, 'CRYPTOUPDATEACCOUNT', 'ACCOUNT'),
       (16, 'FILEAPPEND', 'FILE'),
       (17, 'FILECREATE', 'FILE'),
       (18, 'FILEDELETE', 'FILE'),
       (19, 'FILEUPDATE', 'FILE'),
       (20, 'SYSTEMDELETE', null),
       (21, 'SYSTEMUNDELETE', null),
       (22, 'CONTRACTDELETEINSTANCE', 'CONTRACT'),
       (23, 'FREEZE', null),
       (24, 'CONSENSUSCREATETOPIC', 'TOPIC'),
       (25, 'CONSENSUSUPDATETOPIC', 'TOPIC'),
       (26, 'CONSENSUSDELETETOPIC', 'TOPIC'),
       (27, 'CONSENSUSSUBMITMESSAGE', 'TOPIC'),
       (28, 'UNCHECKEDSUBMIT', null),
       (29, 'TOKENCREATION', 'TOKEN'),
       (31, 'TOKENFREEZE', 'ACCOUNT'),
       (32, 'TOKENUNFREEZE', 'ACCOUNT'),
       (33, 'TOKENGRANTKYC', 'ACCOUNT'),
       (34, 'TOKENREVOKEKYC', 'ACCOUNT'),
       (35, 'TOKENDELETION', 'TOKEN'),
       (36, 'TOKENUPDATE', 'TOKEN'),
       (37, 'TOKENMINT', 'TOKEN'),
       (38, 'TOKENBURN', 'TOKEN'),
       (39, 'TOKENWIPE', 'TOKEN'),
       (40, 'TOKENASSOCIATE', 'ACCOUNT'),
       (41, 'TOKENDISSOCIATE', 'ACCOUNT'),
       (42, 'SCHEDULECREATE', 'SCHEDULE'),
       (43, 'SCHEDULEDELETE', 'SCHEDULE'),
       (44, 'SCHEDULESIGN', 'SCHEDULE'),
       (45, 'TOKENFEESCHEDULEUPDATE', 'TOKEN'),
       (46, 'TOKENPAUSE', 'TOKEN'),
       (47, 'TOKENUNPAUSE', 'TOKEN');

