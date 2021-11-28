class cataloguedb {
  class { 'mysql::server':
    root_password => 'root',
    restart       => true,
  }

  file { 'schema-db.sql':
    ensure => 'file',
    source => 'puppet:///modules/cataloguedb/catalogue-db-import.sql',
    path   => '/usr/local/etc/catalogue-db-import.sql',
    owner  => 'catalogue-user',
    group  => 'catalogue-user',
    mode   => '0744', # Use 0700 if it is sensitive
  }

  mysql::db { 'socksdb':
    user     => 'catalogue_user',
    password => 'default_password',
    host     => '%',
    grant    => ['ALL PRIVILEGES'],
    sql      => '/usr/local/etc/catalogue-db-import.sql'
  }
}
