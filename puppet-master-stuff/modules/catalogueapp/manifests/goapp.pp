define goapp (
  $app_name   = $name,
  $exec_path,
  $exec_args,
  $exec_user  = 'root',
  $exec_group = 'root'
) {
  $serviceUnit = @("SERVICEUNIT")
    [Unit]
    Description=Starting the $app_name

    [Service]
    ExecStart=$exec_path $exec_args
    User=$exec_user
    Group=$exec_group

    [Install]
    WantedBy=multi-user.target
    | SERVICEUNIT

  file { 'service-file':
    ensure  => 'file',
    path    => "/etc/systemd/system/$app_name.service",
    owner   => 'root',
    group   => 'root',
    mode    => '0600',
    content => $serviceUnit
  }

  service { $app_name:
    name    => "$app_name.service",
    ensure  => 'running',
    require => [File[service-file]]
  }

}
