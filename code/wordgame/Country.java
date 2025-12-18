package wordgame;

public class Country
{
    final String name;
    final String capitalCityName;
    final String[] facts;

    public Country(final String name,
                   final String capitalCityName,
                   final String[] facts)
    {
        this.name = name;
        this.capitalCityName = capitalCityName;
        this.facts = facts;
    }

    public String getName()
    {
        return name;
    }

    public String getCapitalCityName()
    {
        return capitalCityName;
    }

    public String[] getFacts()
    {
        return facts;
    }
}
