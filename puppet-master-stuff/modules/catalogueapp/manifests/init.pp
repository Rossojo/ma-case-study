class catalogueapp {
  file { 'catalogue-app':
    ensure => 'file',
    source => 'puppet:///modules/goapp/app',
    path   => '/usr/local/bin/catalogue-app',
    owner  => 'catalogue-user',
    group  => 'catalogue-user',
    mode   => '0700',
  }

  goapp { 'catalogue-app':
    exec_path  => '/usr/local/bin/catalogue-app',
    exec_args  => '-port=8080 -DSN="catalogue_user:default_password@tcp(localhost:3306)/socksdb"',
    exec_user  => 'catalogue-user',
    exec_group => 'catalogue-user',
    require    => [File[catalogue-app]],
  }
}
