package com.example.junit5starter;

import com.example.junit5starter.extension.GlobalExtension;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith({GlobalExtension.class})
public abstract class TestBase {

}
