class catalogueapp {
  file { 'catalogue-app':
    ensure => 'file',
    source => 'puppet:///modules/catalogueapp/app',
    path   => '/usr/local/bin/catalogue-app',
    owner  => 'catalogue-user',
    group  => 'catalogue-user',
    mode   => '0700',
    require => [File[catalogue-images]],
  }

  file { 'catalogue-images-parent':
    ensure => 'directory',
    path   => '/opt/catalogue',
    owner  => 'catalogue-user',
    group  => 'catalogue-user',
    mode   => '0400'
  }

  file { 'catalogue-images':
    ensure  => 'directory',
    source  => 'puppet:///modules/catalogueapp/images',
    path    => '/opt/catalogue/images',
    recurse => true,
    owner   => 'catalogue-user',
    group   => 'catalogue-user',
    mode    => '0400',
    require => [File[catalogue-images-parent]],
  }

  catalogueapp::goapp { 'catalogue-app':
    exec_path  => '/usr/local/bin/catalogue-app',
    exec_args  =>
      '-port=8080 -DSN="catalogue_user:default_password@tcp(localhost:3306)/socksdb" -images "/opt/catalogue/images"',
    exec_user  => 'catalogue-user',
    exec_group => 'catalogue-user',
    require    => [File[catalogue-app]],
  }
}
