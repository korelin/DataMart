package ru.korelin.ivan

object Scripts {

  val flatHistoryScript: String =
    """
      |with time as (select tsb ts from v_flat_accounts union select tsb from v_flat_cards union select tsb from v_flat_savings_accounts)
      |select t.ts,
      |a.id,
      |a.name,
      |a.address,
      |a.phone_number,
      |a.email,
      |a.account_id,
      |a.card_id,
      |a.savings_account_id,
      |c.id,
      |c.card_number,
      |c.monthly_limit,
      |c.status,
      |c.card_id,
      |c.credit_used,
      |s.id,
      |s.savings_account_id,
      |s.status,
      |s.interest_rate_percent,
      |s.balance
      |from time t
      |inner join v_flat_accounts a on t.ts between a.tsb and a.tse
      |left join v_flat_cards c on a.card_id = c.card_id and
      |t.ts between c.tsb and c.tse
      |left join v_flat_savings_accounts s on a.savings_account_id = s.savings_account_id and
      |t.ts between s.tsb and s.tse
      |order by ts
      |""".stripMargin

  val preAggScript: String =
    """
      |select
      |ts,
      |credit_used - lag(credit_used) over (partition by account_id order by ts) card,
      |balance - lag(balance) over (partition by account_id order by ts) save,
      |case when credit_used - lag(credit_used) over (partition by account_id order by ts) != 0 then 1 else 0 end c,
      |case when balance - lag(balance) over (partition by account_id order by ts) != 0 then 1 else 0 end b,
      |account_id account_id
      |from v_flatHistory
      |""".stripMargin
}
