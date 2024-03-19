package com.c20g.labs.agency.util;

import java.util.Locale;

import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CounterpointDBNamingStrategy extends PhysicalNamingStrategyStandardImpl {

    private static final Logger LOGGER = LoggerFactory.getLogger(CounterpointDBNamingStrategy.class);

    public static final CounterpointDBNamingStrategy INSTANCE = new CounterpointDBNamingStrategy();
    
    @Override
    public Identifier toPhysicalTableName(Identifier name, JdbcEnvironment context) {
        String tblName = addUnderscores(name.getText());
        LOGGER.debug("Naming table " + name.getText() + " as " + tblName);
        return new Identifier(tblName, name.isQuoted());
    }

    @Override
    public Identifier toPhysicalColumnName(Identifier name, JdbcEnvironment context) {
        return new Identifier(addUnderscores(name.getText()), name.isQuoted());
    }


    protected static String addUnderscores(String name) {
        final StringBuilder buf = new StringBuilder( name.replace('.', '_') );
        for (int i=1; i<buf.length()-1; i++) {
            if (Character.isLowerCase( buf.charAt(i-1) ) &&
                    Character.isUpperCase( buf.charAt(i) ) &&
                    Character.isLowerCase( buf.charAt(i+1) )) {
                buf.insert(i++, '_');
            }
        }
        return buf.toString().toUpperCase(Locale.ROOT);
    }
    
}
